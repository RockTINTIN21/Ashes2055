package com.ashes2055.client.renderer;

import com.ashes2055.entity.raider.AbstractRaiderEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RaiderRenderer<T extends AbstractRaiderEntity> extends HumanoidMobRenderer<T, PlayerModel<T>> {
    private final ResourceLocation texture;

    public RaiderRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }
}
