package com.ashes2055;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.ashes2055.entity.ModEntities;
import com.ashes2055.item.ModItems;
import com.ashes2055.item.ModCreativeTabs;

/**
 * Main mod class for Ashes2055.
 * Mod creation year: 2025
 */
@Mod(Ashes2055.MOD_ID)
public class Ashes2055 {
    public static final String MOD_ID = "ashes2055";

    public Ashes2055() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
    }
}
