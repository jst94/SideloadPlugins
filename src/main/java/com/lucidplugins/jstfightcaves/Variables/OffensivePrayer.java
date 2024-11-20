package com.lucidplugins.jstfightcaves.Variables;

import net.runelite.api.Prayer;

public enum OffensivePrayer {
    RIGOUR(Prayer.RIGOUR),
    EAGLE_EYE(Prayer.EAGLE_EYE);

    public final Prayer prayer;

    private OffensivePrayer(Prayer prayer) {
        this.prayer = prayer;
    }

    Prayer getPrayer() {
        return this.prayer;
    }
}
