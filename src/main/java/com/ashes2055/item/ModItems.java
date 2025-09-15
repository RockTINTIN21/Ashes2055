package com.ashes2055.item;

import com.ashes2055.Ashes2055;
import com.ashes2055.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ashes2055.MOD_ID);

    public static final RegistryObject<Item> RAIDER_SPAWN_EGG = ITEMS.register("raider_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.RAIDER, 0x544F39, 0x8E8C71,
                    new Item.Properties()));

    public static final RegistryObject<Item> RAIDER_SNIPER_SPAWN_EGG = ITEMS.register("raider_sniper_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.RAIDER_SNIPER, 0x3A2F24, 0xC9C7B1,
                    new Item.Properties()));
}
