package jstscsur;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("jstscur")
public interface JstScurConfig extends Config {

    @ConfigItem(
        keyName = "usePrivateInstance",
        name = "Use Private Instance",
        description = "If enabled, will use private instance for faster kills. Otherwise uses public instance for more XP.",
        position = 0
    )
    default boolean usePrivateInstance() {
        return false;
    }

    @ConfigItem(
        keyName = "minEatHP",
        name = "Minimum HP to Eat",
        description = "Eat food when HP falls below this value",
        position = 1
    )
    default int minEatHP() {
        return 65;
    }

    @ConfigItem(
        keyName = "minPrayerPoints",
        name = "Minimum Prayer Points",
        description = "Drink prayer potion when points fall below this value",
        position = 2
    )
    default int minPrayerPoints() {
        return 30;
    }

    @ConfigItem(
        keyName = "foodIds",
        name = "Food IDs",
        description = "IDs of food items to use, comma separated",
        position = 3
    )
    default String foodIds() {
        return "385,379"; // Shark, Lobster
    }

    @ConfigItem(
        keyName = "prayerPotionIds", 
        name = "Prayer Potion IDs",
        description = "IDs of prayer restore potions to use, comma separated",
        position = 4
    )
    default String prayerPotionIds() {
        return "2434,139,141,143"; // Prayer potion(4,3,2,1)
    }

    @ConfigItem(
        keyName = "quickPrayers",
        name = "Use Quick Prayers",
        description = "Use quick prayers instead of individual prayer switching",
        position = 5
    )
    default boolean useQuickPrayers() {
        return false;
    }

    @ConfigItem(
        keyName = "safeSpotTile",
        name = "Safe Spot World Point",
        description = "World point to use as safe spot (x,y,z)",
        position = 6
    )
    default String safeSpotTile() {
        return "3417,9950,0"; // Default safe spot in the boss room
    }
}
