package com.ashes2055.client.renderer;

import com.ashes2055.Ashes2055;
import com.ashes2055.entity.RaiderEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RaiderRenderer extends HumanoidMobRenderer<RaiderEntity, PlayerModel<RaiderEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Ashes2055.MOD_ID, "textures/entity/raider/raider_stormtrooper.png");

    public RaiderRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RaiderEntity entity) {
        return TEXTURE;
    }
}
