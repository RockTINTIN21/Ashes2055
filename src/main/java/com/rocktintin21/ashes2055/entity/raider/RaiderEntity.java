package com.rocktintin21.ashes2055.entity.raider;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;

import java.util.function.Supplier;

/**
 * Base raider entity using TACZ firearms.
 */
public class RaiderEntity extends AbstractSkeleton {
    public RaiderEntity(EntityType<? extends RaiderEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack stack = getMainHandItem();
        if (stack.getItem() instanceof AbstractGunItem gun && !level().isClientSide) {
            ShooterDataHolder data = new ShooterDataHolder();
            data.initialData();
            data.currentGunItem = () -> stack;
            Supplier<Float> zero = () -> 0f;
            if (gun.canReload(this, stack)) {
                gun.startReload(data, stack, this);
            } else {
                gun.shoot(data, stack, zero, zero, this);
            }
        }
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }
}
