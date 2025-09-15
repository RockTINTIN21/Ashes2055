package com.ashes2055.event;

import com.ashes2055.Ashes2055;
import com.ashes2055.client.renderer.RaiderRenderer;
import com.ashes2055.entity.ModEntities;
import com.ashes2055.entity.raider.RaiderEntity;
import com.ashes2055.entity.raider.RaiderSniperEntity;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ashes2055.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {
    private static final ResourceLocation RAIDER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Ashes2055.MOD_ID, "textures/entity/raider/raider.png");

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RAIDER.get(),
                context -> new RaiderRenderer<RaiderEntity>(context, RAIDER_TEXTURE));
        event.registerEntityRenderer(ModEntities.RAIDER_SNIPER.get(),
                context -> new RaiderRenderer<RaiderSniperEntity>(context, RAIDER_TEXTURE));
        event.registerEntityRenderer(ModEntities.BULLET.get(), NoopRenderer::new);
    }
}
