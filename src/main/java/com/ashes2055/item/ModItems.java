package com.ashes2055.item;

import com.ashes2055.Ashes2055;
import com.ashes2055.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ashes2055.MOD_ID);

    // Яйцо призыва для вашего RaiderEntity
    public static final RegistryObject<Item> RAIDER_SPAWN_EGG = ITEMS.register(
            "raider_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.RAIDER,
                    0x3A3A3A,
                    0xC43D3D,
                    new Item.Properties()
            )
    );

    private ModItems() {}
}
