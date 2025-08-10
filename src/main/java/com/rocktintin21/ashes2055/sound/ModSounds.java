package com.rocktintin21.ashes2055.sound;

import com.rocktintin21.ashes2055.Ashes2055Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Ashes2055Mod.MODID);

    public static final RegistryObject<SoundEvent> RAIDER_LIFE = SOUND_EVENTS.register("raider.life",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Ashes2055Mod.MODID, "raider.life")));

    public static final RegistryObject<SoundEvent> RAIDER_AGGRESSION = SOUND_EVENTS.register("raider.aggression",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Ashes2055Mod.MODID, "raider.aggression")));

    public static final RegistryObject<SoundEvent> RAIDER_DEATH = SOUND_EVENTS.register("raider.death",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Ashes2055Mod.MODID, "raider.death")));

    public static final RegistryObject<SoundEvent> RAIDER_RELOAD = SOUND_EVENTS.register("raider.reload",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Ashes2055Mod.MODID, "raider.reload")));

    public static final RegistryObject<SoundEvent> RAIDER_HURT = SOUND_EVENTS.register("raider.hurt",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Ashes2055Mod.MODID, "raider.hurt")));

    public static final RegistryObject<SoundEvent> RAIDER_ENEMY_DOWN = SOUND_EVENTS.register("raider.enemy_down",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Ashes2055Mod.MODID, "raider.enemy_down")));
}

