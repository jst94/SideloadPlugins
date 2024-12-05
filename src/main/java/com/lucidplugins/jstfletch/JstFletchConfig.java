package com.lucidplugins.jstfletch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ConfigGroup("jstfletch")
public interface JstFletchConfig extends Config {
    
    enum LogType {
        NORMAL(1511, "Normal", 1),
        OAK(1521, "Oak", 15),
        WILLOW(1519, "Willow", 30),
        MAPLE(1517, "Maple", 45),
        YEW(1515, "Yew", 60),
        MAGIC(1513, "Magic", 75);

        @Getter private final int itemId;
        @Getter private final String name;
        @Getter private final int levelRequired;

        LogType(int itemId, String name, int levelRequired) {
            this.itemId = itemId;
            this.name = name;
            this.levelRequired = levelRequired;
        }
    }

    enum FletchType {
        ARROW_SHAFT("Arrow Shaft", 1, 52, 15),
        SHORTBOW("Shortbow", 5, 841, 20),
        LONGBOW("Longbow", 10, 839, 25);

        @Getter private final String name;
        @Getter private final int levelRequired;
        @Getter private final int productId;
        @Getter private final int interfaceIndex;

        FletchType(String name, int levelRequired, int productId, int interfaceIndex) {
            this.name = name;
            this.levelRequired = levelRequired;
            this.productId = productId;
            this.interfaceIndex = interfaceIndex;
        }
    }

    @ConfigItem(
        keyName = "logType",
        name = "Log Type",
        description = "The type of log to use for fletching",
        position = 0
    )
    default LogType logType() {
        return LogType.NORMAL;
    }

    @ConfigItem(
        keyName = "fletchType",
        name = "Fletch Type",
        description = "What to fletch from the logs",
        position = 1
    )
    default FletchType fletchType() {
        return FletchType.ARROW_SHAFT;
    }

    @ConfigItem(
        keyName = "useGe",
        name = "Use Grand Exchange",
        description = "Buy logs from GE when out of supplies",
        position = 2
    )
    default boolean useGe() {
        return false;
    }

    @ConfigItem(
        keyName = "geQuantity",
        name = "GE Buy Quantity",
        description = "How many logs to buy at once from GE",
        position = 3
    )
    @Range(min = 1, max = 10000)
    default int geQuantity() {
        return 1000;
    }

    @ConfigItem(
        keyName = "started",
        name = "Start/Stop",
        description = "Start or stop the plugin",
        position = 4
    )
    default boolean started() {
        return false;
    }
}
