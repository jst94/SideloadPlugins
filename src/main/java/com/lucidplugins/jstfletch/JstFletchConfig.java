package com.lucidplugins.jstfletch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("jstfletch")
public interface JstFletchConfig extends Config {
    @ConfigItem(
        keyName = "logType",
        name = "Log Type",
        description = "The type of log to use for fletching (e.g., normal logs, oak logs, etc.)",
        position = 0
    )
    default int getLogType() {
        return 1511; // Default to normal logs
    }

    @ConfigItem(
        keyName = "buyQuantity",
        name = "Buy Quantity",
        description = "The quantity of logs to buy from the Grand Exchange",
        position = 1
    )
    default int getBuyQuantity() {
        return 1000; // Default to 1000
    }

    @ConfigItem(
        keyName = "buyPrice",
        name = "Buy Price",
        description = "The price to buy logs at from the Grand Exchange",
        position = 2
    )
    default int getBuyPrice() {
        return 100; // Default to 100 gp
    }

    @ConfigItem(
        keyName = "useWikiPrices",
        name = "Use Wiki Prices",
        description = "Use prices fetched from the OSRS wiki instead of fixed prices",
        position = 3
    )
    default boolean useWikiPrices() {
        return false;
    }

    @ConfigItem(
        keyName = "knifeItemId",
        name = "Knife Item ID",
        description = "The item ID of the knife to use",
        position = 4
    )
    default int getKnifeItemId() {
        return 946; // Default knife item ID
    }

    @ConfigItem(
        keyName = "startFletching",
        name = "Start Fletching",
        description = "Enable to start the automated fletching",
        position = 98
    )
    default boolean startFletching() {
        return false;
    }

    @ConfigItem(
        keyName = "stopFletching",
        name = "Stop Fletching",
        description = "Enable to stop the automated fletching",
        position = 99
    )
    default boolean stopFletching() {
        return false;
    }
}
