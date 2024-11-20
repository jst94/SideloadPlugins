package com.lucidplugins.jstfightcaves.Variables;

public enum CavesFoodType {
    CHEESE_POTATO(6705),
    TUNA_POTATO(7060),
    SHARK(385),
    KARAMBWAN(3144),
    DARK_CRAB(11936),
    MANTA_RAY(391),
    SARADOMIN_BREW(6685),
    ANGLERFISH(13441);

    private final int foodTypeId;

    private CavesFoodType(int foodTypeId) {
        this.foodTypeId = foodTypeId;
    }

    public int getFoodTypeId() {
        return this.foodTypeId;
    }
}
