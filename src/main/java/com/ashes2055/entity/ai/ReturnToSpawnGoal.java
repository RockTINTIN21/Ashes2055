package com.ashes2055.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.function.Supplier;

public class ReturnToSpawnGoal<T extends Mob> extends Goal {
    private final T mob;
    private final Supplier<BlockPos> homeSupplier;
    private final double speed;
    private final double arriveRadiusSq;

    public ReturnToSpawnGoal(T mob, Supplier<BlockPos> homeSupplier, double speed, double arriveRadius) {
        this.mob = mob;
        this.homeSupplier = homeSupplier;
        this.speed = speed;
        this.arriveRadiusSq = arriveRadius * arriveRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Возвращаемся домой, только если цели нет и «дом» известен и мы далеко
        if (mob.getTarget() != null) return false;
        BlockPos home = homeSupplier.get();
        if (home == null) return false;
        return home.distToCenterSqr(mob.position()) > arriveRadiusSq;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        BlockPos home = homeSupplier.get();
        if (home == null) return;
        mob.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, speed);
    }
}
