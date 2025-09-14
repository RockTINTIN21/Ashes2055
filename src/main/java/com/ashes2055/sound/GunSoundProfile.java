// src/main/java/com/ashes2055/sound/GunSoundProfile.java
package com.ashes2055.sound;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public final class GunSoundProfile {
    public final RegistryObject<SoundEvent> fire1p;     // для локального стрелка (камера игрока)
    public final RegistryObject<SoundEvent> fire3p;     // ближняя третье-лицо
    public final RegistryObject<SoundEvent> far;        // средняя дистанция
    public final RegistryObject<SoundEvent> veryFar;    // дальняя дистанция
    public final RegistryObject<SoundEvent> reload;     // перезарядка
    public final double nearMax;    // блоки: граница 3p
    public final double midMax;     // блоки: граница far
    public final double farMax;     // блоки: граница veryfar
    public final double maxHear;    // блоки: докуда вообще слышно

    public GunSoundProfile(
            RegistryObject<SoundEvent> fire1p,
            RegistryObject<SoundEvent> fire3p,
            RegistryObject<SoundEvent> far,
            RegistryObject<SoundEvent> veryFar,
            RegistryObject<SoundEvent> reload,
            double nearMax, double midMax, double farMax, double maxHear
    ) {
        this.fire1p = fire1p;
        this.fire3p = fire3p;
        this.far = far;
        this.veryFar = veryFar;
        this.reload = reload;
        this.nearMax = nearMax;
        this.midMax = midMax;
        this.farMax = farMax;
        this.maxHear = maxHear;
    }

    public SoundEvent pickShot(double distance, boolean isLocalShooter) {
        if (isLocalShooter) return fire1p.get();
        if (distance <= nearMax) return fire3p.get();
        if (distance <= midMax)  return far.get();
        if (distance <= farMax)  return veryFar.get();
        return null; // слишком далеко — не играем
    }

    public SoundEvent pickReload(double distance) {
        return (distance <= nearMax) ? reload.get() : null; // перезарядку слышно ближе
    }
}
