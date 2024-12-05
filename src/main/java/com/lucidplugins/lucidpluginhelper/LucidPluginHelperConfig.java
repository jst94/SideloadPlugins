package com.lucidplugins.lucidpluginhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lucidpluginhelper")
public interface LucidPluginHelperConfig extends Config {
    @ConfigItem(
            keyName = "enableMenuEntryLogging",
            name = "Log Menu Entries",
            description = "Enable logging of menu entries",
            position = 0
    )
    default boolean enableMenuEntryLogging() {
        return true;
    }

    @ConfigItem(
            keyName = "enableGameObjectLogging",
            name = "Log Game Objects",
            description = "Enable logging of game objects",
            position = 1
    )
    default boolean enableGameObjectLogging() {
        return true;
    }

    @ConfigItem(
            keyName = "enableNpcLogging",
            name = "Log NPCs",
            description = "Enable logging of NPCs",
            position = 2
    )
    default boolean enableNpcLogging() {
        return true;
    }

    @ConfigItem(
            keyName = "enableGroundItemLogging",
            name = "Log Ground Items",
            description = "Enable logging of ground items",
            position = 3
    )
    default boolean enableGroundItemLogging() {
        return true;
    }

    @ConfigItem(
            keyName = "enableWidgetLogging",
            name = "Log Widgets",
            description = "Enable logging of widgets",
            position = 4
    )
    default boolean enableWidgetLogging() {
        return true;
    }

    @ConfigItem(
            keyName = "enablePacketLogging",
            name = "Log Packets",
            description = "Enable logging of client packets",
            position = 5
    )
    default boolean enablePacketLogging() {
        return true;
    }

    @ConfigItem(
            keyName = "showDebugOverlay",
            name = "Show Debug Overlay",
            description = "Display debug information as an overlay",
            position = 6
    )
    default boolean showDebugOverlay() {
        return false;
    }
}
