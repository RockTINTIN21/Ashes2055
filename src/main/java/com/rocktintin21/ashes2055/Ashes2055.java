package com.rocktintin21.ashes2055;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.rocktintin21.ashes2055.entity.ModEntities;
import com.rocktintin21.ashes2055.entity.raider.RaiderEntity;

@Mod(Ashes2055.MOD_ID)
public class Ashes2055 {
    public static final String MOD_ID = "ashes2055";

    public Ashes2055() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addEntityAttributes);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.RAIDER.get(), RaiderEntity.createAttributes().build());
    }
}
