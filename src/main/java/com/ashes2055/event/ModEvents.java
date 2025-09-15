package com.ashes2055.event;

import com.ashes2055.Ashes2055;
import com.ashes2055.entity.ModEntities;
import com.ashes2055.entity.raider.RaiderEntity;
import com.ashes2055.entity.raider.RaiderSniperEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ashes2055.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.RAIDER.get(), RaiderEntity.createAttributes().build());
        event.put(ModEntities.RAIDER_SNIPER.get(), RaiderSniperEntity.createAttributes().build());
    }
}
