package com.lucidplugins.jstfletch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("jstfletch")
public interface JstFletchConfig extends Config {
    @ConfigItem(
        keyName = "logType",
        name = "Log Type",
        description = "The type of log to use for fletching"
    )
    default int getLogType() {
        return 1511; // Default to normal logs
    }

    @ConfigItem(
        keyName = "fletchingOption",
        name = "Fletching Option",
        description = "The fletching option to choose"
    )
    default int getFletchingOption() {
        return 1; // Default to first option
    }

    @ConfigItem(
        keyName = "buyQuantity",
        name = "Buy Quantity",
        description = "The quantity of logs to buy from the Grand Exchange"
    )
    default int getBuyQuantity() {
        return 1000; // Default to 1000
    }

    @ConfigItem(
        keyName = "buyPrice",
        name = "Buy Price",
        description = "The price to buy logs at from the Grand Exchange"
    )
    default int getBuyPrice() {
        return 100; // Default to 100 gp
    }

    @ConfigItem(
        keyName = "startButton",
        name = "Start Fletching",
        description = "Start the automated fletching"
    )
    default boolean startFletching() {
        return false;
    }

    @ConfigItem(
        keyName = "stopButton",
        name = "Stop Fletching",
        description = "Stop the automated fletching"
    )
    default boolean stopFletching() {
        return false;
    }

    @ConfigItem(
        keyName = "useWikiPrices",
        name = "Use Wiki Prices",
        description = "Use prices fetched from the OSRS wiki instead of fixed prices"
    )
    default boolean useWikiPrices() {
        return false;
    }
}
