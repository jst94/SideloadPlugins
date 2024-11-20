package com.lucidplugins.api.spells;

/**
 * Taken from marcojacobsNL
 * https://github.com/marcojacobsNL/runelite-plugins/blob/master/src/main/java/com/koffee/KoffeeUtils/Runes.java
 */

import com.google.common.collect.ImmutableMap;
import net.runelite.api.ItemID;

import java.util.Map;

public enum Runes {
    AIR(ItemID.AIR_RUNE, 1),
    MIND(ItemID.MIND_RUNE, 2),
    WATER(ItemID.WATER_RUNE, 3),
    EARTH(ItemID.EARTH_RUNE, 4),
    FIRE(ItemID.FIRE_RUNE, 5),
    BODY(ItemID.BODY_RUNE, 6),
    COSMIC(ItemID.COSMIC_RUNE, 7),
    CHAOS(ItemID.CHAOS_RUNE, 8),
    NATURE(ItemID.NATURE_RUNE, 9),
    LAW(ItemID.LAW_RUNE, 10),
    DEATH(ItemID.DEATH_RUNE, 11),
    ASTRAL(ItemID.ASTRAL_RUNE, 12),
    BLOOD(ItemID.BLOOD_RUNE, 13),
    SOUL(ItemID.SOUL_RUNE, 14),
    WRATH(ItemID.WRATH_RUNE, 15);

    private final int itemId;
    private final int runeId;

    private static final Map<Integer, Integer> RUNE_IDS;

    static {
        ImmutableMap.Builder<Integer, Integer> builder = ImmutableMap.builder();
        for (Runes rune : values()) {
            builder.put(rune.itemId, rune.runeId);
        }
        RUNE_IDS = builder.build();
    }

    Runes(int itemId, int runeId) {
        this.itemId = itemId;
        this.runeId = runeId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getRuneId() {
        return runeId;
    }

    public static Map<Integer, Integer> getRuneIds() {
        return RUNE_IDS;
    }

    public static int getVarbitIndexForItemId(int itemId) {
        return RUNE_IDS.getOrDefault(itemId, -1);
    }
}