package com.lucidplugins.jstfightcaves.Variables;

public enum CavesSpecWeapons {
    MAGIC_SHORTBOW(861, 50),
    MAGIC_SHORTBOW_I(12788, 50),
    TOXIC_BLOWPIPE(12926, 50),
    BLAZING_BLOWPIPE(28688, 50);

    private final int itemId;
    private final int specRequired;

    private CavesSpecWeapons(int itemId, int specRequired) {
        this.itemId = itemId;
        this.specRequired = specRequired;
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getSpecRequired() {
        return this.specRequired;
    }
}
