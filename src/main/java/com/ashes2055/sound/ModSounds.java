package com.ashes2055.sound;

import com.ashes2055.Ashes2055;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Ashes2055.MOD_ID);

    // RAIDERS
    public static final RegistryObject<SoundEvent> RAIDER_LIFE        = register("raider.life");
    public static final RegistryObject<SoundEvent> RAIDER_AGGRESSION  = register("raider.aggression");
    public static final RegistryObject<SoundEvent> RAIDER_DEATH       = register("raider.death");
    public static final RegistryObject<SoundEvent> RAIDER_RELOAD      = register("raider.reload");
    public static final RegistryObject<SoundEvent> RAIDER_HURT        = register("raider.hurt");
    public static final RegistryObject<SoundEvent> RAIDER_ENEMY_DOWN  = register("raider.enemy_down");

    //HIT
    public static final SoundEvent FLESH_HIT     = register("flesh_hit");

    // AK-47
    public static final RegistryObject<SoundEvent> AK47_FIRE_1P     = register("ak47.fire_1p");
    public static final RegistryObject<SoundEvent> AK47_FIRE_3P     = register("ak47.fire_3p");
    public static final RegistryObject<SoundEvent> AK47_FAR         = register("ak47.far");
    public static final RegistryObject<SoundEvent> AK47_VERYFAR     = register("ak47.veryfar");
    public static final RegistryObject<SoundEvent> AK47_RELOAD_EMPTY= register("ak47.reload_empty");

    // Homemade shotgun
    public static final RegistryObject<SoundEvent> HSHOTGUN_FIRE_1P      = register("homemade_shotgun.fire_1p");
    public static final RegistryObject<SoundEvent> HSHOTGUN_FIRE_3P      = register("homemade_shotgun.fire_3p");
    public static final RegistryObject<SoundEvent> HSHOTGUN_FAR          = register("homemade_shotgun.far");
    public static final RegistryObject<SoundEvent> HSHOTGUN_VERYFAR      = register("homemade_shotgun.veryfar");
    public static final RegistryObject<SoundEvent> HSHOTGUN_RELOAD_EMPTY = register("homemade_shotgun.reload_empty");

    // RPK
    public static final RegistryObject<SoundEvent> RPK_FIRE_1P      = register("rpk.fire_1p");
    public static final RegistryObject<SoundEvent> RPK_FIRE_3P      = register("rpk.fire_3p");
    public static final RegistryObject<SoundEvent> RPK_FAR          = register("rpk.far");
    public static final RegistryObject<SoundEvent> RPK_VERYFAR      = register("rpk.veryfar");
    public static final RegistryObject<SoundEvent> RPK_RELOAD_EMPTY = register("rpk.reload_empty");

    // SKS
    public static final RegistryObject<SoundEvent> SKS_FIRE_1P      = register("sks.fire_1p");
    public static final RegistryObject<SoundEvent> SKS_FIRE_3P      = register("sks.fire_3p");
    public static final RegistryObject<SoundEvent> SKS_FAR          = register("sks.far");
    public static final RegistryObject<SoundEvent> SKS_VERYFAR      = register("sks.veryfar");
    public static final RegistryObject<SoundEvent> SKS_RELOAD_EMPTY = register("sks.reload_empty");

    private static RegistryObject<SoundEvent> register(String path) {
        return SOUND_EVENTS.register(path, () ->
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Ashes2055.MOD_ID, path)));
    }
}
