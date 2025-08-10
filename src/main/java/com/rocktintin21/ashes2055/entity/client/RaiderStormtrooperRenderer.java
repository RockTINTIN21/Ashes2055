package com.rocktintin21.ashes2055.entity.client;

import com.rocktintin21.ashes2055.entity.RaiderStormtrooper;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class RaiderStormtrooperRenderer extends HumanoidMobRenderer<RaiderStormtrooper, PlayerModel<RaiderStormtrooper>> {
    private static final ResourceLocation SKIN = DefaultPlayerSkin.getDefaultSkin();

    public RaiderStormtrooperRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(RaiderStormtrooper entity) {
        return SKIN;
    }
}