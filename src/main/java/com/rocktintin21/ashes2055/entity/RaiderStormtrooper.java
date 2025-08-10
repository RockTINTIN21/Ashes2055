package com.rocktintin21.ashes2055.entity;

import com.rocktintin21.ashes2055.voice.VoiceLineType;
import com.rocktintin21.ashes2055.voice.VoiceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class RaiderStormtrooper extends AbstractSkeleton implements FactionEntity {

    // --- configurable combat parameters ---
    /** size of magazine in arrows */
    private int magazineSize = 10;
    /** cooldown between individual shots, in ticks */
    private int fireRateTicks = 20;
    /** time to reload a magazine, in ticks */
    private int reloadTicks = 40;

    private static final int VOICE_INTERVAL_TICKS = 160;

    private int bulletsRemaining = magazineSize;
    private int fireCooldown = 0;
    private int reloadCooldown = 0;
    private int lifeSoundCooldown = VOICE_INTERVAL_TICKS;
    private int aggroSoundCooldown = 0;
    private boolean hasAggroTarget = false;

    public RaiderStormtrooper(EntityType<? extends RaiderStormtrooper> type, Level level) {
        super(type, level);
    }

    @Override
    public Faction getFaction() {
        return Faction.RAIDERS;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> !this.isAlliedTo(entity)));
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof FactionEntity fe) {
            return fe.getFaction() == this.getFaction();
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof FactionEntity fe && fe.getFaction() == this.getFaction()) {
            return false;
        }
        boolean result = super.hurt(source, amount);
        if (result && this.random.nextFloat() < 0.1f) {
            VoiceManager.play(this, VoiceLineType.HURT);
        }
        return result;
    }

    /** Prevent this mob from despawning when difficulty set to peaceful */
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }

    @Override
    public void tick() {
        super.tick();
        if (fireCooldown > 0) fireCooldown--;
        if (reloadCooldown > 0) {
            reloadCooldown--;
            if (reloadCooldown == 0) {
                bulletsRemaining = magazineSize;
            }
        }

        if (!level().isClientSide) {
            if (getTarget() != null) {
                if (!hasAggroTarget) {
                    hasAggroTarget = true;
                    if (aggroSoundCooldown <= 0) {
                        if (this.random.nextBoolean() && isNearestToTarget(getTarget())) {
                            VoiceManager.play(this, VoiceLineType.AGGRESSION);
                        }
                        aggroSoundCooldown = VOICE_INTERVAL_TICKS;
                    }
                } else if (--aggroSoundCooldown <= 0) {
                    aggroSoundCooldown = VOICE_INTERVAL_TICKS;
                    if (this.random.nextBoolean()) {
                        VoiceManager.play(this, VoiceLineType.AGGRESSION);
                    }
                }
                lifeSoundCooldown = VOICE_INTERVAL_TICKS;
            } else {
                hasAggroTarget = false;
                if (aggroSoundCooldown > 0) aggroSoundCooldown--;
                if (--lifeSoundCooldown <= 0) {
                    lifeSoundCooldown = VOICE_INTERVAL_TICKS;
                    if (this.random.nextBoolean()) {
                        VoiceManager.play(this, VoiceLineType.LIFE);
                    }
                }
            }
        }
    }

    private boolean isNearestToTarget(LivingEntity target) {
        double myDist = this.distanceToSqr(target);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(16))) {
            if (entity == this) continue;
            if (entity instanceof FactionEntity fe && fe.getFaction() == this.getFaction()) {
                if (entity.distanceToSqr(target) < myDist) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (reloadCooldown > 0 || fireCooldown > 0 || bulletsRemaining <= 0) {
            if (bulletsRemaining <= 0 && reloadCooldown == 0) {
                reloadCooldown = reloadTicks;
                VoiceManager.play(this, VoiceLineType.RELOAD);
            }
            return;
        }
        super.performRangedAttack(target, distanceFactor);
        bulletsRemaining--;
        fireCooldown = fireRateTicks;
        if (bulletsRemaining <= 0) {
            reloadCooldown = reloadTicks;
            VoiceManager.play(this, VoiceLineType.RELOAD);
        }
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PLAYER_BREATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        var soundType = state.getSoundType(this.level(), pos, this);
        SoundEvent sound = soundType.getStepSound();
        this.playSound(sound, soundType.getVolume() * 0.15F, soundType.getPitch());
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        VoiceManager.play(this, VoiceLineType.DEATH);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        boolean result = super.killedEntity(level, entity);
        if (entity instanceof Player && this.random.nextBoolean()) {
            VoiceManager.play(this, VoiceLineType.ENEMY_DOWN);
        }
        return result;
    }

    // setters to tweak combat parameters per mob instance
    public void setMagazineSize(int magazineSize) {
        this.magazineSize = magazineSize;
        this.bulletsRemaining = magazineSize;
    }

    public void setFireRateTicks(int fireRateTicks) {
        this.fireRateTicks = fireRateTicks;
    }

    public void setReloadTicks(int reloadTicks) {
        this.reloadTicks = reloadTicks;
    }
}