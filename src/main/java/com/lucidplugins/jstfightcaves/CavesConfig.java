package com.lucidplugins.jstfightcaves;

import com.lucidplugins.jstfightcaves.Variables.*;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("cavesplugin")
public interface CavesConfig extends Config {
    @ConfigItem(
            keyName = "pluginEnabled",
            name = "Enable Plugin",
            description = "Enable or disable the plugin",
            position = 0
    )
    default boolean pluginEnabled() {
        return false;
    }

    @ConfigItem(
            keyName = "toggle",
            name = "Toggle",
            description = "Toggle the plugin",
            position = 1
    )
    default String toggle() {
        return "Alt + 1";
    }

    @ConfigItem(
            keyName = "useSpec",
            name = "Use Special Attack",
            description = "Use special attack during the fight",
            position = 2
    )
    default boolean useSpec() {
        return false;
    }

    @ConfigItem(
            keyName = "useBlowpipe",
            name = "Use Blowpipe",
            description = "Use blowpipe during the fight",
            position = 3
    )
    default boolean useBlowpipe() {
        return false;
    }

    @ConfigItem(
            keyName = "prayerStyle",
            name = "Prayer Style",
            description = "Choose your prayer flicking style",
            position = 4
    )
    default CavesPrayerStyle prayerStyle() {
        return CavesPrayerStyle.NORMAL;
    }

    @ConfigItem(
            keyName = "offensivePrayer",
            name = "Offensive Prayer",
            description = "Choose your offensive prayer",
            position = 5
    )
    default OffensivePrayer offensivePrayer() {
        return OffensivePrayer.RIGOUR;
    }

    @ConfigItem(
            keyName = "specWeapon",
            name = "Special Attack Weapon",
            description = "Choose your special attack weapon",
            position = 6
    )
    default CavesSpecWeapons specWeapon() {
        return CavesSpecWeapons.TOXIC_BLOWPIPE;
    }

    @ConfigItem(
            keyName = "specHitpointsThreshold",
            name = "Spec HP Threshold",
            description = "Hitpoints threshold for using special attack",
            position = 7
    )
    @Range(min = 1, max = 99)
    default int specHitpointsThreshold() {
        return 50;
    }

    @ConfigItem(
            keyName = "eatThreshold",
            name = "Eat Threshold",
            description = "Hitpoints threshold for eating food",
            position = 8
    )
    @Range(min = 1, max = 99)
    default int eatThreshold() {
        return 50;
    }

    @ConfigItem(
            keyName = "minBoost",
            name = "Min Boost",
            description = "Minimum boost level before drinking potions",
            position = 9
    )
    @Range(min = 1, max = 19)
    default int minBoost() {
        return 5;
    }

    @ConfigItem(
            keyName = "foodType",
            name = "Food Type",
            description = "Choose your food type",
            position = 10
    )
    default CavesFoodType foodType() {
        return CavesFoodType.SARADOMIN_BREW;
    }

    @ConfigItem(
            keyName = "restorePotionType",
            name = "Restore Potion Type",
            description = "Choose your restore potion type",
            position = 11
    )
    default CaveRestorePotion restorePotionType() {
        return CaveRestorePotion.SUPER_RESTORE;
    }

    @ConfigItem(
            keyName = "prayerRestorationThreshold",
            name = "Prayer Restoration Threshold",
            description = "Prayer points threshold for drinking restore potions",
            position = 12
    )
    @Range(min = 1, max = 99)
    default int prayerRestorationThreshold() {
        return 30;
    }

    @ConfigItem(
            keyName = "dartType",
            name = "Dart Type",
            description = "Choose your dart type for the blowpipe",
            position = 13
    )
    default DartType dartType() {
        return DartType.DRAGON;
    }

    @ConfigItem(
            keyName = "minScalesInBlowpipe",
            name = "Min Scales in Blowpipe",
            description = "Minimum amount of scales in blowpipe before recharging",
            position = 14
    )
    @Range(min = 100, max = 16383)
    default int minScalesInBlowpipe() {
        return 1000;
    }

    @ConfigItem(
            keyName = "minDartsInBlowpipe",
            name = "Min Darts in Blowpipe",
            description = "Minimum amount of darts in blowpipe before recharging",
            position = 15
    )
    @Range(min = 100, max = 16383)
    default int minDartsInBlowpipe() {
        return 1000;
    }

    @ConfigItem(
            keyName = "uiLayoutOption",
            name = "UI Layout",
            description = "Choose your UI layout",
            position = 16
    )
    default UiLayoutOption uiLayoutOption() {
        return UiLayoutOption.FULL;
    }

    @ConfigItem(
            keyName = "foodAmounts",
            name = "Food Amount",
            description = "Amount of food to withdraw",
            position = 17
    )
    @Range(min = 1, max = 28)
    default int foodAmounts() {
        return 12;
    }

    @ConfigItem(
            keyName = "restoreAmounts",
            name = "Restore Amount",
            description = "Amount of restore potions to withdraw",
            position = 18
    )
    @Range(min = 1, max = 28)
    default int restoreAmounts() {
        return 8;
    }

    @ConfigItem(
            keyName = "rangingPotions",
            name = "Ranging Potions",
            description = "Amount of ranging potions to withdraw",
            position = 19
    )
    @Range(min = 1, max = 28)
    default int rangingPotions() {
        return 2;
    }

    @ConfigItem(
            keyName = "staminaAmounts",
            name = "Stamina Amount",
            description = "Amount of stamina potions to withdraw",
            position = 20
    )
    @Range(min = 0, max = 28)
    default int staminaAmounts() {
        return 2;
    }

    @ConfigItem(
            keyName = "dartsForBlowpipe",
            name = "Darts for Blowpipe",
            description = "Amount of darts to withdraw for blowpipe",
            position = 21
    )
    @Range(min = 100, max = 16383)
    default int dartsForBlowpipe() {
        return 2000;
    }

    @ConfigItem(
            keyName = "scalesForBlowpipe",
            name = "Scales for Blowpipe",
            description = "Amount of scales to withdraw for blowpipe",
            position = 22
    )
    @Range(min = 100, max = 16383)
    default int scalesForBlowpipe() {
        return 2000;
    }

    @ConfigItem(
            keyName = "useStaminas",
            name = "Use Stamina Potions",
            description = "Use stamina potions during the fight",
            position = 23
    )
    default boolean useStaminas() {
        return true;
    }
}
