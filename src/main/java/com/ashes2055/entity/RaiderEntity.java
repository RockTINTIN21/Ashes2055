package com.ashes2055.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
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

/**
 * Raider assault mob using a bullet projectile and leather armor.
 */
public class RaiderEntity extends FactionMob implements RangedAttackMob {
    // Configurable parameters
    public static final double MAX_HEALTH = 20.0D;
    public static final double MOVE_SPEED = 0.25D;
    public static final double ARMOR = 2.0D;
    public static final double AGGRO_DISTANCE = 50.0D;
    public static final float ATTACK_DISTANCE = 50.0F;
    public static final int ATTACK_INTERVAL = 20; // ticks between shots
    public static final int RELOAD_TIME = 40; // ticks to reload
    public static final int MAGAZINE_SIZE = 30;
    public static final float BULLET_DAMAGE = 4.0F;
    public static final SoundEvent SHOOT_SOUND = SoundEvents.CROSSBOW_SHOOT;

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

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (reloadTicks > 0) {
            return;
        }
        BulletEntity bullet = new BulletEntity(this.level(), this);
        bullet.setDamage(BULLET_DAMAGE);
        double d0 = target.getX() - this.getX();
        double d1 = target.getEyeY() - bullet.getY();
        double d2 = target.getZ() - this.getZ();
        bullet.shoot(d0, d1, d2, BulletEntity.SPEED, 0.0F);
        this.level().addFreshEntity(bullet);
        this.level().playSound(null, this, SHOOT_SOUND, SoundSource.HOSTILE, 1.0F, 1.0F);
        shotsFired++;
        if (shotsFired % MAGAZINE_SIZE == 0) {
            reloadTicks = RELOAD_TIME;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (reloadTicks > 0) {
            if (reloadTicks == RELOAD_TIME) {
                broadcast("магазин вытащен");
            } else if (reloadTicks == RELOAD_TIME / 2) {
                broadcast("новый магазин вставлен");
            } else if (reloadTicks == 1) {
                broadcast("звук передергивания затвора");
            }
            reloadTicks--;
        }
    }

    private void broadcast(String message) {
        if (!this.level().isClientSide && this.level().getServer() != null) {
            this.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        SpawnGroupData spawnData, CompoundTag data) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, reason, spawnData, data);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.populateDefaultEquipmentEnchantments(level.getRandom(), difficulty);
        return spawnGroupData;
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
