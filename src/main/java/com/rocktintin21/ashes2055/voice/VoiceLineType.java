package com.rocktintin21.ashes2055.voice;

public enum VoiceLineType {
    RELOAD("reload"),
    RELOAD_MAG_OUT("reload_mag_out"),
    RELOAD_MAG_IN("reload_mag_in"),
    LIFE("life"),
    AGGRESSION("aggression"),
    DEATH("death"),
    HURT("hurt"),
    ENEMY_DOWN("enemy_down");

    private final String id;

    VoiceLineType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}