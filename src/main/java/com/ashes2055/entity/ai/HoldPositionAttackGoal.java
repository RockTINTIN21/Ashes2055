package com.ashes2055.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.RangedAttackMob;

import java.util.function.Supplier;

/**
 * Keeps the mob anchored to a specific position, returning to it if pushed away.
 */
public class HoldPositionAttackGoal<T extends PathfinderMob & RangedAttackMob> extends AbstractGunAttackGoal<T> {
    private final Supplier<BlockPos> anchorSupplier;
    private final double anchorRadiusSq;
    private int recalcCooldown;

    public HoldPositionAttackGoal(T mob, double moveSpeedModifier, int attackInterval, float attackRadius,
                                  Supplier<BlockPos> anchorSupplier, double anchorRadius) {
        super(mob, moveSpeedModifier, attackInterval, attackRadius);
        this.anchorSupplier = anchorSupplier;
        this.anchorRadiusSq = anchorRadius * anchorRadius;
    }

    @Override
    protected void updateMovement(LivingEntity target, double distanceSq, boolean hasLineOfSight) {
        BlockPos anchor = this.anchorSupplier.get();
        if (anchor == null) {
            return;
        }

        double distanceFromAnchorSq = this.mob.distanceToSqr(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
        if (distanceFromAnchorSq > this.anchorRadiusSq) {
            if (this.recalcCooldown > 0) {
                --this.recalcCooldown;
            } else {
                this.recalcCooldown = 10;
                this.mob.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, this.getMoveSpeedModifier());
            }
        } else if (hasLineOfSight) {
            this.mob.getNavigation().stop();
        }
    }
}
