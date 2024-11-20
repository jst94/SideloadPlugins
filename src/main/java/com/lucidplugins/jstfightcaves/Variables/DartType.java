package com.lucidplugins.jstfightcaves.Variables;

public enum DartType {
    BRONZE(806),
    IRON(807),
    STEEL(808),
    MITHRIL(809),
    ADAMANT(810),
    RUNE(811),
    AMETHYST(25849),
    DRAGON(11230);

    private final int dartItemId;

    private DartType(int dartItemId) {
        this.dartItemId = dartItemId;
    }

    public int getDartItemId() {
        return this.dartItemId;
    }
}
