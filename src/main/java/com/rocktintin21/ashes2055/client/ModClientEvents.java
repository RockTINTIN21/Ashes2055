package com.rocktintin21.ashes2055.client;

import com.rocktintin21.ashes2055.Ashes2055;
import com.rocktintin21.ashes2055.client.renderer.RaiderRenderer;
import com.rocktintin21.ashes2055.entity.ModEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ashes2055.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RAIDER.get(), RaiderRenderer::new);
    }
}
