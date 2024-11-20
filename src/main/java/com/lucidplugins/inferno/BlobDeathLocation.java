package com.lucidplugins.inferno;

import net.runelite.api.coords.WorldPoint;

public class BlobDeathLocation {
    private final WorldPoint location;
    private int ticksUntilDone;
    private int fillAlpha;

    public BlobDeathLocation(WorldPoint location, int ticksUntilDone) {
        this.location = location;
        this.ticksUntilDone = ticksUntilDone;
        this.fillAlpha = 255;
    }

    public WorldPoint getLocation() {
        return location;
    }

    public int getTicksUntilDone() {
        return ticksUntilDone;
    }

    public void decrementTicks() {
        if (ticksUntilDone > 0) {
            ticksUntilDone--;
        }
    }

    public int getFillAlpha() {
        return fillAlpha;
    }

    public void setFillAlpha(int fillAlpha) {
        this.fillAlpha = fillAlpha;
    }

    public void updateFillAlpha(int maxTicks) {
        if (maxTicks > 0) {
            this.fillAlpha = (int) (255.0 * ticksUntilDone / maxTicks);
        }
    }
}
