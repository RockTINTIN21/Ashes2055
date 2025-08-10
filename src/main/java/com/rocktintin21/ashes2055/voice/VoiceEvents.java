package com.rocktintin21.ashes2055.voice;

import com.rocktintin21.ashes2055.Ashes2055Mod;
import com.rocktintin21.ashes2055.entity.RaiderStormtrooper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ashes2055Mod.MODID)
public class VoiceEvents {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        var source = event.getSource().getEntity();
        if (source instanceof RaiderStormtrooper raider && event.getEntity() instanceof net.minecraft.world.entity.player.Player) {
            if (raider.getRandom().nextBoolean()) {
                VoiceManager.play(raider, VoiceLineType.ENEMY_DOWN);
            }
        }
    }
}