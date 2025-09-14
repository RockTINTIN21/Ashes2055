package com.ashes2055.entity;

import com.ashes2055.Ashes2055;
import com.ashes2055.entity.projectile.BulletEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ashes2055.MOD_ID);

    public static final RegistryObject<EntityType<RaiderEntity>> RAIDER = ENTITY_TYPES.register(
            "raider",
            () -> EntityType.Builder.of(RaiderEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build(Ashes2055.MOD_ID + ":raider")
    );

    public static final RegistryObject<EntityType<BulletEntity>> BULLET = ENTITY_TYPES.register(
            "bullet",
            () -> EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                    .sized(0.125f, 0.125f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(Ashes2055.MOD_ID + ":bullet")
    );
}
