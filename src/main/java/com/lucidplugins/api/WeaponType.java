package com.lucidplugins.api;

import lombok.Getter;
import net.runelite.api.Prayer;

@Getter
public enum WeaponType {
    MELEE(Prayer.PROTECT_FROM_MELEE, Prayer.PIETY),
    RANGED(Prayer.PROTECT_FROM_MISSILES, Prayer.RIGOUR),
    MAGIC(Prayer.PROTECT_FROM_MAGIC, Prayer.AUGURY),
    OTHER(Prayer.PROTECT_FROM_MELEE, Prayer.PIETY);

    private final Prayer protectionPrayer;
    private final Prayer offensivePrayer;

    WeaponType(Prayer protectionPrayer, Prayer offensivePrayer) {
        this.protectionPrayer = protectionPrayer;
        this.offensivePrayer = offensivePrayer;
    }

    public Prayer getProtectionPrayer() {
        return protectionPrayer;
    }

    public Prayer getOffensivePrayer() {
        return offensivePrayer;
    }
}
