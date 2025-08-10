package com.rocktintin21.ashes2055.entity;

public enum Faction {
    RAIDERS("raider"),
    FACTION_B("faction_b"),
    FACTION_C("faction_c");

    private final String id;

    Faction(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
