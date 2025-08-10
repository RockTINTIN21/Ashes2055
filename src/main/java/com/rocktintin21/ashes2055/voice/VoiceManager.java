package com.rocktintin21.ashes2055.voice;

import com.rocktintin21.ashes2055.entity.FactionEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class VoiceManager {
    public static void play(LivingEntity entity, VoiceLineType type) {
        if (entity.level().isClientSide) {
            return;
        }
        String faction = entity instanceof FactionEntity fe ? fe.getFaction().name().toLowerCase() : "unknown";
        String message = "[" + faction + "] " + type.name().toLowerCase();
        for (Player player : entity.level().players()) {
            player.sendSystemMessage(Component.literal(message));
        }
    }
}
