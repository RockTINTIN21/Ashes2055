package com.ashes2055.entity;

import com.ashes2055.entity.projectile.BulletEntity;
import com.ashes2055.net.Net;
import com.ashes2055.net.ShotSfxS2C;
import com.ashes2055.sound.GunTypes;
import com.ashes2055.sound.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.Item;
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
import net.minecraftforge.network.PacketDistributor.TargetPoint;

import javax.annotation.Nullable;
import com.ashes2055.entity.ai.MaintainDistanceRangedGoal;
import com.ashes2055.entity.ai.ReturnToSpawnGoal;
import com.ashes2055.combat.GunSfx;

import net.minecraft.core.BlockPos;

public class RaiderEntity extends FactionMob implements RangedAttackMob {
    private GunTypes gunType = GunTypes.AK47;

    public static final double MAX_HEALTH = 25.0D;
    public static final double MOVE_SPEED = 0.35D;
    public static final double ARMOR = 2.0D;
    public static final double AGGRO_DISTANCE = 70.0D;
    public static final float ATTACK_DISTANCE = 25.0F;
    public static final int ATTACK_INTERVAL = 1;
    public static final int RELOAD_TIME = 40;
    public static final int MAGAZINE_SIZE = 30;
    public static float BULLET_DAMAGE = 6.0F;

    private static final int VOICE_PERIOD_TICKS = 100;
    private int voiceCooldown = 0;
    private boolean reloadVoicePlayed = false;

    private int shotsFired;
    private int reloadTicks;

    private BlockPos spawnPos;

    private static final double INDOOR_RANGE = 3.5;
    private static final int ENV_CHECK_EVERY_TICKS = 20;

    private boolean isCrampedCached = false;
    private int envCheckCooldown = 0;

    public BlockPos getHome() {
        return spawnPos;
    }

    public double getPreferredCombatRange() {
        if (--envCheckCooldown <= 0) {
            isCrampedCached = computeIsCramped();
            // немного «шумим» период, чтобы разные мобы не синхронизировались
            envCheckCooldown = ENV_CHECK_EVERY_TICKS + this.getRandom().nextInt(ENV_CHECK_EVERY_TICKS);
        }
        return isCrampedCached ? INDOOR_RANGE : ATTACK_DISTANCE;
    }

    public RaiderEntity(EntityType<? extends RaiderEntity> entityType, Level level) {
        super(entityType, level, Faction.RAIDERS);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MaintainDistanceRangedGoal<>(this, 1, ATTACK_INTERVAL, this::getPreferredCombatRange));
        this.goalSelector.addGoal(2, new ReturnToSpawnGoal<>(this, this::getHome, 1.0D, 2.0D));

        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, true, mob -> !this.isAlliedTo(mob)));


        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
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

        bullet.setDamage(this.getBulletDamage());

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
                GunSfx.sendShot(this.level(), this, this.gunType, true);
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

    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, reason, data, tag);
        this.spawnPos = this.blockPosition();
        this.setMaxUpStep(1.0F); // переступает через блоки, визуально ускоряет перемещение

        Item gunItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun"));
        if (gunItem != null) {
            ItemStack ak = new ItemStack(gunItem);

            CompoundTag nbt = ak.getOrCreateTag();
            nbt.putString("GunId", "tacz:ak47");

            this.setItemSlot(EquipmentSlot.MAINHAND, ak);
            this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }

        return result;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }

    private boolean isSolidAt(BlockPos p) {
        var lvl = this.level();
        var st = lvl.getBlockState(p);
        // «Солидный» блок — не воздух и с непустой коллизией
        return !st.isAir() && !st.getCollisionShape(lvl, p).isEmpty();
    }

    /**
     * Грубая, но дешёвая эвристика «тесного помещения».
     * Возвращает true, если над головой низкий потолок/плотные стены, и false, если «улица».
     */
    private boolean computeIsCramped() {
        var lvl = this.level();
        var pos = this.blockPosition();

        // Быстрый выход: если видно небо — почти наверняка улица
        // (используем .above(), чтобы избежать артефактов блока в ногах)
        if (lvl.canSeeSkyFromBelowWater(pos.above())) {
            return false;
        }

        // Проверка «потолка»: 4 блока вверх — насколько плотно
        int roofSolid = 0;
        for (int dy = 1; dy <= 4; dy++) {
            if (isSolidAt(pos.above(dy))) roofSolid++;
        }

        // Минимальный «зазор над головой»: 2 блока над головой должны быть свободны
        boolean noHeadroom = isSolidAt(pos.above(1)) || isSolidAt(pos.above(2));

        // Плотность «стен» вокруг: кольцо радиуса 2 на уровне ног
        int samples = 0, solids = 0;
        final int r = 2;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx == 0 && dz == 0) continue;
                // берём только «кольцо» и крест — информативно и недорого
                if (Math.max(Math.abs(dx), Math.abs(dz)) != r && !(dx == 0 || dz == 0)) continue;
                samples++;
                if (isSolidAt(pos.offset(dx, 0, dz))) solids++;
            }
        }
        double solidRatio = samples == 0 ? 0.0 : (double) solids / samples;

        // Итоговая эвристика: низкий потолок, плохой зазор, плотные стены — считаем «теснотой»
        return roofSolid >= 2 || noHeadroom || solidRatio >= 0.40;
    }

}
