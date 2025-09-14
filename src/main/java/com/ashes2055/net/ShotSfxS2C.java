// src/main/java/com/ashes2055/net/ShotSfxS2C.java
package com.ashes2055.net;

import com.ashes2055.sound.GunTypes;
import com.ashes2055.sound.GunSoundProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ShotSfxS2C(double x, double y, double z, int gunTypeOrdinal, int shooterId, boolean reload) {
    public static void encode(ShotSfxS2C m, FriendlyByteBuf buf) {
        buf.writeDouble(m.x); buf.writeDouble(m.y); buf.writeDouble(m.z);
        buf.writeVarInt(m.gunTypeOrdinal);
        buf.writeVarInt(m.shooterId);
        buf.writeBoolean(m.reload);
    }
    public static ShotSfxS2C decode(FriendlyByteBuf buf) {
        return new ShotSfxS2C(buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }
    public static void handle(ShotSfxS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) return;
            var mc = Minecraft.getInstance();
            var level = mc.level;
            if (level == null || mc.player == null) return;

            var type = GunTypes.values()[msg.gunTypeOrdinal];
            var prof = type.profile;

            // локальный стрелок?
            Entity shooter = level.getEntity(msg.shooterId);
            boolean isLocalShooter = (shooter == mc.player);

            double dist = mc.player.position().distanceTo(new Vec3(msg.x, msg.y, msg.z));

            SoundEvent ev = msg.reload
                    ? prof.pickReload(dist)
                    : prof.pickShot(dist, isLocalShooter);

            if (ev != null) {
                float pitch = 0.97f + level.random.nextFloat() * 0.06f;
                // локальное воспроизведение на клиенте
                level.playLocalSound(msg.x, msg.y, msg.z, ev, SoundSource.HOSTILE,
                        1.0f, pitch, false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
