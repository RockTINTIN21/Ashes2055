package com.ashes2055.combat;

import com.ashes2055.net.Net;
import com.ashes2055.net.ShotSfxS2C;
import com.ashes2055.sound.GunTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;

public final class GunSfx {
    private GunSfx() {}

    public static void sendShot(Level level, LivingEntity shooter, GunTypes gunType, boolean reload) {
        if (level.isClientSide) return;

        double radius = reload ? gunType.profile.nearMax : gunType.maxHearing();
        Net.CH.send(
                PacketDistributor.NEAR.with(() -> new TargetPoint(
                        shooter.getX(), shooter.getY(), shooter.getZ(), radius, level.dimension())),
                new ShotSfxS2C(
                        shooter.getX(), shooter.getEyeY(), shooter.getZ(),
                        gunType.ordinal(), shooter.getId(), reload
                )
        );
    }
}
