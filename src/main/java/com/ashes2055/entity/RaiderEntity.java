package com.ashes2055.entity;

import com.ashes2055.entity.projectile.BulletEntity;
import com.ashes2055.net.Net;
import com.ashes2055.net.ShotSfxS2C;
import com.ashes2055.sound.GunTypes;
import com.ashes2055.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;

import javax.annotation.Nullable;

/**
 * Raider assault mob using a bullet projectile and leather armor.
 */
public class RaiderEntity extends FactionMob implements RangedAttackMob {
    private GunTypes gunType = GunTypes.AK47; // выберите дефолт или задавайте при спавне
    // Configurable parameters
    public static final double MAX_HEALTH = 20.0D;
    public static final double MOVE_SPEED = 0.25D;
    public static final double ARMOR = 2.0D;
    public static final double AGGRO_DISTANCE = 50.0D;
    public static final float ATTACK_DISTANCE = 50.0F;
    public static final int ATTACK_INTERVAL = 3; // ticks between shots
    public static final int RELOAD_TIME = 40; // ticks to reload
    public static final int MAGAZINE_SIZE = 30;
    public static float BULLET_DAMAGE = 4.0F;
    public static final SoundEvent SHOOT_SOUND = SoundEvents.CROSSBOW_SHOOT;

    private static final int VOICE_PERIOD_TICKS = 100; // 5 сек при 20 TPS
    private int voiceCooldown = 0;
    private boolean reloadVoicePlayed = false;

    private int shotsFired;
    private int reloadTicks;

    public RaiderEntity(EntityType<? extends RaiderEntity> entityType, Level level) {
        super(entityType, level, Faction.RAIDERS);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, MOVE_SPEED, ATTACK_INTERVAL, ATTACK_DISTANCE));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, true, mob -> !this.isAlliedTo(mob)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.FOLLOW_RANGE, AGGRO_DISTANCE)
                .add(Attributes.ARMOR, ARMOR);
    }

    /** Публичный геттер — пуля возьмёт урон отсюда */
    public float getBulletDamage() {
        return BULLET_DAMAGE;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("BulletDamage", BULLET_DAMAGE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BulletDamage")) {
            BULLET_DAMAGE = tag.getFloat("BulletDamage");
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (reloadTicks > 0) return;

        // 1) Создаём пулю с небольшим выносом вперёд по направлению взгляда
        BulletEntity bullet = new BulletEntity(this.level(), this, this.getX(), this.getEyeY() - 0.1, this.getZ());
        Vec3 look = this.getLookAngle().normalize();
        bullet.setPos(bullet.getX() + look.x * 0.25, bullet.getY() + look.y * 0.25, bullet.getZ() + look.z * 0.25);

        bullet.setDamage(this.getBulletDamage())
                .setHitSound(net.minecraft.sounds.SoundEvents.ANVIL_LAND);

        double dx = target.getX()    - this.getX();
        double dy = target.getEyeY() - this.getEyeY();
        double dz = target.getZ()    - this.getZ();
        double len2 = dx*dx + dy*dy + dz*dz;

        if (len2 < 1.0E-7) bullet.shoot(look.x, look.y, look.z, BulletEntity.SPEED, 0.0F);
        else               bullet.shoot(dx, dy, dz, BulletEntity.SPEED, 0.0F);

        if (!this.level().isClientSide) {
            this.level().addFreshEntity(bullet);

            // 2) Вместо прямого playSound — рассылаем один "шаблонный" пакет о выстреле.
            //    Клиент сам выберет подвид (1p/3p/far/veryfar) по дистанции.
            double radius = this.gunType.maxHearing(); // докуда в принципе слышно это оружие
            Net.CH.send(
                    PacketDistributor.NEAR.with(() -> new TargetPoint(
                            this.getX(), this.getY(), this.getZ(), radius, this.level().dimension())),
                    new ShotSfxS2C(this.getX(), this.getEyeY(), this.getZ(),
                            this.gunType.ordinal(), this.getId(), /*reload=*/false)
            );
        }

        // 3) Учёт магазина и старт перезарядки с озвучкой
        shotsFired++;
        if (shotsFired % MAGAZINE_SIZE == 0) {
            reloadTicks = RELOAD_TIME;

            if (!this.level().isClientSide) {
                // Перезарядку имеет смысл слать на меньший радиус (nearMax профиля)
                double r = this.gunType.profile.nearMax;
                Net.CH.send(
                        PacketDistributor.NEAR.with(() -> new TargetPoint(
                                this.getX(), this.getY(), this.getZ(), r, this.level().dimension())),
                        new ShotSfxS2C(this.getX(), this.getEyeY(), this.getZ(),
                                this.gunType.ordinal(), this.getId(), /*reload=*/true)
                );
            }
        }
    }


    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        // --- перезарядка ---
        if (reloadTicks > 0) {
            reloadTicks--; // критично: выходим из перезарядки
            if (reloadTicks == 0) {
                shotsFired = 0;        // опционально: начать новый магазин с нуля
                reloadVoicePlayed = false; // позволить заново проиграть озвучку при следующей перезарядке
            }
        }

        // Обновляем кулдаун фраз
        if (voiceCooldown > 0) voiceCooldown--;

        boolean inCombat = this.getTarget() != null && this.getTarget().isAlive();

        if (voiceCooldown == 0) {
            if (inCombat) {
                if (this.getRandom().nextFloat() < 0.5f) {
                    level().playSound(null, this, ModSounds.RAIDER_AGGRESSION.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            } else {
                if (this.getRandom().nextFloat() < 0.5f) {
                    level().playSound(null, this, ModSounds.RAIDER_LIFE.get(), SoundSource.HOSTILE, 0.9f, 1.0f);
                }
            }
            voiceCooldown = VOICE_PERIOD_TICKS;
        }

        // Озвучка перезарядки (один раз при входе в перезарядку)
        if (reloadTicks > 0 && !reloadVoicePlayed) {
            level().playSound(null, this, ModSounds.RAIDER_RELOAD.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
            reloadVoicePlayed = true;
        }
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        super.playHurtSound(source);
    }


    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            level().playSound(null, this, ModSounds.RAIDER_DEATH.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        super.die(source);
    }

    /** Вызываем, когда рейдер точно убил цель. */
    public void onEnemyDown() {
        if (!level().isClientSide) {
            level().playSound(null, this, ModSounds.RAIDER_ENEMY_DOWN.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        }
    }

    private void broadcast(String message) {
        if (!this.level().isClientSide && this.level().getServer() != null) {
            this.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        var out = super.finalizeSpawn(level, diff, reason, data, tag);
        float r = this.getRandom().nextFloat();
        this.gunType = GunTypes.AK47;
        return out;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }
}
