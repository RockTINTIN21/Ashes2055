package com.rocktintin21.ashes2055.client.renderer;

import com.rocktintin21.ashes2055.entity.raider.RaiderEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class RaiderRenderer extends HumanoidMobRenderer<RaiderEntity, HumanoidModel<RaiderEntity>> {
    public RaiderRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RaiderEntity entity) {
        return DefaultPlayerSkin.getDefaultSkin();
    }
}
