package com.ashes2055.event;

import com.ashes2055.Ashes2055;
import com.ashes2055.entity.ModEntities;
import com.ashes2055.client.renderer.RaiderRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ashes2055.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RAIDER.get(), RaiderRenderer::new);
        event.registerEntityRenderer(ModEntities.BULLET.get(), NoopRenderer::new);

    }
}
