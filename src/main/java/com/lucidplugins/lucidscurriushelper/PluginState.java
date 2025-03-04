package com.lucidplugins.lucidscurriushelper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PluginState {
    private boolean justDodged;
    private boolean needsRestock;
    private boolean isRestocking;
    private boolean returningToInstance;
    private boolean needToEatShark;
    private boolean needToEnterManhole;

    private int lastDodgeTick;
    private int lastRatTick;
    private int lastActivateTick;
    private int lastEatTick;
    private int lastPrayerRestoreTick;

    public void reset() {
        justDodged = false;
        needsRestock = false;
        isRestocking = false;
        returningToInstance = false;
        needToEatShark = false;
        needToEnterManhole = false;
    }
}