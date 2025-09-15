package com.ashes2055.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

import java.util.EnumSet;

/**
 * Base AI for gun-wielding mobs. Handles aiming and firing cadence while delegating
 * movement strategy to subclasses.
 */
public abstract class AbstractGunAttackGoal<T extends PathfinderMob & RangedAttackMob> extends Goal {
    protected final T mob;
    private final double moveSpeedModifier;
    private final float attackRadius;
    private final float attackRadiusSqr;
    private final int attackInterval;

    private int attackTime = -1;
    private int seeTime;

    protected AbstractGunAttackGoal(T mob, double moveSpeedModifier, int attackInterval, float attackRadius) {
        this.mob = mob;
        this.moveSpeedModifier = moveSpeedModifier;
        this.attackInterval = attackInterval;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        super.start();
        this.attackTime = 0;
        this.seeTime = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }

        double distanceSq = this.mob.distanceToSqr(target);
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);

        this.updateMovement(target, distanceSq, hasLineOfSight);
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }

        this.attackTime--;
        if (this.attackTime <= 0) {
            if (hasLineOfSight && distanceSq <= this.attackRadiusSqr) {
                float distance = (float) Math.sqrt(distanceSq);
                float distanceFactor = Mth.clamp(distance / this.attackRadius, 0.1F, 1.0F);
                this.mob.performRangedAttack(target, distanceFactor);
                this.attackTime = this.attackInterval;
            }
        }

        this.mob.setAggressive(this.seeTime > 0 && distanceSq <= this.attackRadiusSqr);
    }

    protected double getMoveSpeedModifier() {
        return this.moveSpeedModifier;
    }

    protected float getAttackRadius() {
        return this.attackRadius;
    }

    protected float getAttackRadiusSqr() {
        return this.attackRadiusSqr;
    }

    protected int getAttackInterval() {
        return this.attackInterval;
    }

    /**
     * Allows subclasses to control how the mob moves while engaging a target.
     */
    protected abstract void updateMovement(LivingEntity target, double distanceSq, boolean hasLineOfSight);
}
