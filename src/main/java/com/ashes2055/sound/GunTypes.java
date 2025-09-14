// src/main/java/com/ashes2055/sound/GunTypes.java
package com.ashes2055.sound;

import static com.ashes2055.sound.ModSounds.*;

public enum GunTypes {
    AK47(new GunSoundProfile(
            AK47_FIRE_1P, AK47_FIRE_3P, AK47_FAR, AK47_VERYFAR, AK47_RELOAD_EMPTY,
            22, 60, 110, 160
    )),
    RPK(new GunSoundProfile(
            RPK_FIRE_1P, RPK_FIRE_3P, RPK_FAR, RPK_VERYFAR, RPK_RELOAD_EMPTY,
            22, 60, 110, 160
    )),
    HSHOTGUN(new GunSoundProfile(
            HSHOTGUN_FIRE_1P, HSHOTGUN_FIRE_3P, HSHOTGUN_FAR, HSHOTGUN_VERYFAR, HSHOTGUN_RELOAD_EMPTY,
            22, 60, 110, 160
    )),
    SKS(new GunSoundProfile(
            SKS_FIRE_1P, SKS_FIRE_3P, SKS_FAR, SKS_VERYFAR, SKS_RELOAD_EMPTY,
            22, 60, 110, 160
    )),
    ;

    public final GunSoundProfile profile;
    GunTypes(GunSoundProfile p) { this.profile = p; }

    public double maxHearing() { return profile.maxHear; }
}