package com.lucidplugins.lucidscurriushelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("lucid-scurrius-helper")
public interface LucidScurriusHelperConfig extends Config
{
    @ConfigItem(
            name = "Auto-dodge ceiling",
            description = "Automatically dodges the falling ceiling attack",
            position = 0,
            keyName = "autoDodge"
    )
    default boolean autoDodge()
    {
        return true;
    }

    @ConfigItem(
            name = "Auto-attack",
            description = "Automatically attacks Scurrius and rats",
            position = 1,
            keyName = "autoAttack"
    )
    default boolean autoAttack()
    {
        return true;
    }

    @ConfigItem(
            name = "Auto-pray",
            description = "Automatically switches prayers based on incoming attacks",
            position = 2,
            keyName = "autoPray"
    )
    default boolean autoPray()
    {
        return true;
    }

    @ConfigItem(
            name = "Prioritize rats",
            description = "Prioritize killing rats over attacking Scurrius",
            position = 3,
            keyName = "prioritizeRats"
    )
    default boolean prioritizeRats()
    {
        return true;
    }

    @ConfigItem(
            name = "Auto-eat",
            description = "Automatically eat food when health is low",
            position = 4,
            keyName = "autoEat"
    )
    default boolean autoEat()
    {
        return true;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            name = "Eat at HP",
            description = "Eat food when HP falls below this value",
            position = 5,
            keyName = "eatAtHp"
    )
    default int eatAtHp()
    {
        return 50;
    }

    @ConfigItem(
            name = "Food IDs",
            description = "IDs of food to eat (comma separated)",
            position = 6,
            keyName = "foodIds"
    )
    default String foodIds()
    {
        return "385,379"; // Shark, Lobster
    }

    @ConfigItem(
            name = "Auto-restore prayer",
            description = "Automatically drink prayer potions when prayer is low",
            position = 7,
            keyName = "autoPrayerRestore"
    )
    default boolean autoPrayerRestore()
    {
        return true;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            name = "Restore prayer at",
            description = "Restore prayer when points fall below this value",
            position = 8,
            keyName = "restorePrayerAt"
    )
    default int restorePrayerAt()
    {
        return 30;
    }

    @ConfigItem(
            name = "Prayer potion IDs",
            description = "IDs of prayer potions to use (comma separated)",
            position = 9,
            keyName = "prayerPotionIds"
    )
    default String prayerPotionIds()
    {
        return "2434,139,141,143"; // Prayer potion(4,3,2,1)
    }

    @ConfigItem(
            name = "Auto-restock",
            description = "Automatically restock supplies when low",
            position = 10,
            keyName = "autoRestock"
    )
    default boolean autoRestock()
    {
        return true;
    }

    @ConfigItem(
            name = "Bank location",
            description = "Location of the bank to use for restocking",
            position = 11,
            keyName = "bankLocation"
    )
    default String bankLocation()
    {
        return "3214,3377,0"; // Varrock west bank
    }

    @ConfigItem(
            name = "Varrock teleport tab ID",
            description = "ID of the Varrock teleport tab to use",
            position = 12,
            keyName = "varrockTabId"
    )
    default int varrockTabId()
    {
        return 8007; // Varrock teleport tab
    }
}