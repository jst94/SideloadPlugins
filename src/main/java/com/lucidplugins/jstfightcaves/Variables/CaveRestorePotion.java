package com.lucidplugins.jstfightcaves.Variables;

public enum CaveRestorePotion {
    PRAYER_POTION(2434),
    SUPER_RESTORE(3024);

    private final int restorePotionId;

    private CaveRestorePotion(int restorePotionId) {
        this.restorePotionId = restorePotionId;
    }

    public int getRestorePotionId() {
        return this.restorePotionId;
    }
}
