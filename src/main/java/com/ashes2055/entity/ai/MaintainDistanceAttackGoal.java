package com.ashes2055.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.phys.Vec3;

/**
 * Causes the mob to keep a preferred distance from its target, backing away if the
 * target gets too close and closing the gap if the target is too far.
 */
public class MaintainDistanceAttackGoal<T extends PathfinderMob & RangedAttackMob> extends AbstractGunAttackGoal<T> {
    private final float minDistance;
    private final float maxDistance;
    private int repositionCooldown;

    public MaintainDistanceAttackGoal(T mob, double moveSpeedModifier, int attackInterval,
                                      float attackRadius, float minDistance, float maxDistance) {
        super(mob, moveSpeedModifier, attackInterval, attackRadius);
        this.minDistance = minDistance;
        this.maxDistance = Math.max(maxDistance, minDistance + 1.0F);
    }

    @Override
    protected void updateMovement(LivingEntity target, double distanceSq, boolean hasLineOfSight) {
        double minDistanceSq = this.minDistance * this.minDistance;
        double maxDistanceSq = this.maxDistance * this.maxDistance;

        if (distanceSq > maxDistanceSq) {
            this.mob.getNavigation().moveTo(target, this.getMoveSpeedModifier());
            this.repositionCooldown = 0;
            return;
        }

        if (distanceSq < minDistanceSq) {
            if (this.repositionCooldown > 0) {
                --this.repositionCooldown;
            } else {
                this.repositionCooldown = 10;
                Vec3 away = new Vec3(this.mob.getX() - target.getX(), 0.0, this.mob.getZ() - target.getZ());
                if (away.lengthSqr() > 1.0E-4D) {
                    Vec3 direction = away.normalize().scale(this.minDistance * 0.8F);
                    double destinationX = this.mob.getX() + direction.x;
                    double destinationZ = this.mob.getZ() + direction.z;
                    this.mob.getNavigation().moveTo(destinationX, this.mob.getY(), destinationZ, this.getMoveSpeedModifier());
                }
            }
            return;
        }

        if (hasLineOfSight) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(target, this.getMoveSpeedModifier());
        }
    }
}
