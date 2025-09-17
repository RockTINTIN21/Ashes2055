package com.ashes2055.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.DoubleSupplier;

public class MaintainDistanceRangedGoal<T extends Mob & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speed;
    private final int attackIntervalTicks;
    private final DoubleSupplier preferredRangeSupplier;

    private static final double BAND_RATIO = 0.15; // коридор ±15%
    private static final int    REPATH_BASE = 10;  // тики между репафами (грубая экономия)
    private static final int    STUCK_LIMIT = 30;  // тиков без движения — считаем застреванием

    private int attackCooldown;
    private int strafeTicks;
    private float strafeDir = 1f;

    private int repathTicks;
    private int stuckTicks;
    private Vec3 lastPos;

    public MaintainDistanceRangedGoal(T mob, double speed, int attackIntervalTicks, DoubleSupplier preferredRangeSupplier) {
        this.mob = mob;
        this.speed = speed;
        this.attackIntervalTicks = attackIntervalTicks;
        this.preferredRangeSupplier = preferredRangeSupplier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.attackCooldown = 0;
        this.strafeTicks = 0;
        this.repathTicks = 0;
        this.stuckTicks = 0;
        this.lastPos = mob.position();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double desired = preferredRangeSupplier.getAsDouble();
        if (desired <= 0) desired = 16.0; // защита от нуля

        Vec3 self = mob.position();
        Vec3 tgt  = target.position();
        Vec3 toTarget = tgt.subtract(self);
        double dist = toTarget.length();
        boolean hasLOS = mob.getSensing().hasLineOfSight(target);

        // слежение за застреванием
        if (self.distanceToSqr(lastPos) < 0.01) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
            lastPos = self;
        }
        if (stuckTicks > STUCK_LIMIT) {
            // пинок в сторону, чтобы сдвинуться с точки
            Vec3 toward = dist > 1e-5 ? toTarget.scale(1.0 / dist) : new Vec3(1, 0, 0);
            Vec3 left   = new Vec3(-toward.z, 0, toward.x).normalize();
            Vec3 dest   = self.add(left.scale((mob.getRandom().nextBoolean() ? 1 : -1) * 1.2));
            mob.getNavigation().moveTo(dest.x, dest.y, dest.z, speed);
            stuckTicks = 0; // сбросить счётчик; параллельно разрешим стрельбу по LOS ниже
        }

        // коридор дистанции
        double min = desired * (1.0 - BAND_RATIO);
        double max = desired * (1.0 + BAND_RATIO);

        Vec3 toward = dist > 1e-5 ? toTarget.scale(1.0 / dist) : new Vec3(1, 0, 0);
        Vec3 left   = new Vec3(-toward.z, 0, toward.x).normalize();

        // ДВИЖЕНИЕ
        if (dist < min) {
            // слишком близко — отступаем назад и немного «сдвигаемся» в сторону
            double backStep = Math.max(3.0, min - dist + 1.5); // шаг побольше, чтобы патфайндер строил путь
            Vec3 dest = self.subtract(toward.scale(backStep)).add(left.scale(strafeDir * 0.6));
            if (mob.getNavigation().isDone() || (mob.tickCount % 5 == 0)) {
                mob.getNavigation().moveTo(dest.x, dest.y, dest.z, speed);
            }
        } else if (dist > max || !hasLOS) {
            // слишком далеко или нет LOS — идём напрямую к цели
            if (repathTicks-- <= 0 || mob.getNavigation().isDone()) {
                mob.getNavigation().moveTo(target, speed);
                repathTicks = REPATH_BASE + mob.getRandom().nextInt(10);
            }
        } else {
            // в коридоре — круговой «дрейф»
            if (strafeTicks-- <= 0) {
                strafeTicks = 20 + mob.getRandom().nextInt(20);
                strafeDir = mob.getRandom().nextBoolean() ? 1f : -1f;
            }
            Vec3 dest = self.add(left.scale(strafeDir * 1.0));
            if (mob.getNavigation().isDone() || (mob.tickCount % 7 == 0)) {
                mob.getNavigation().moveTo(dest.x, dest.y, dest.z, speed * 0.9);
            }
        }

        // СТРЕЛЬБА
        if (attackCooldown > 0) attackCooldown--;

        // разрешаем огонь при LOS и при любой дистанции не дальше, чем 1.3×нормы
        double shootMax = desired * 1.30;
        if (hasLOS && dist <= shootMax && attackCooldown <= 0) {
            mob.performRangedAttack(target, 1.0f);
            attackCooldown = attackIntervalTicks;
        }
    }
}
