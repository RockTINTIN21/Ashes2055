package com.ashes2055;

import com.ashes2055.entity.ModEntities;
import com.ashes2055.item.ModCreativeTabs;
import com.ashes2055.item.ModItems;
import com.ashes2055.net.Net;
import com.ashes2055.sound.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Ashes2055.MOD_ID)
public class Ashes2055 {
    public static final String MOD_ID = "ashes2055";

    public Ashes2055() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Net.register();

        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
    }
}
