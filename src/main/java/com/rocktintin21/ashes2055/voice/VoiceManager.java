package com.rocktintin21.ashes2055.voice;

import com.rocktintin21.ashes2055.Ashes2055Mod;
import com.rocktintin21.ashes2055.entity.FactionEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class VoiceManager {
    public static void play(LivingEntity entity, VoiceLineType type) {
        if (entity.level().isClientSide) {
            return;
        }
        String faction = entity instanceof FactionEntity fe ? fe.getFaction().getId() : "unknown";
        ResourceLocation id = new ResourceLocation(Ashes2055Mod.MODID, faction + "." + type.getId());
        SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(id);
        if (event != null) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), event, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
}