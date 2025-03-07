package com.lucidplugins.jstdolo;

import net.runelite.client.config.*;

@ConfigGroup("jstdolo")
public interface JstDoloConfig extends Config {
    @ConfigSection(
            name = "Main Account",
            description = "Settings for your main account",
            position = 0
    )
    String mainSection = "Main Account";

    @ConfigSection(
            name = "Alt Account",
            description = "Settings for your alt account",
            position = 1
    )
    String altSection = "Alt Account";

    @ConfigItem(
            keyName = "mainUsername",
            name = "Main Username",
            description = "Username of your main account for identification",
            position = 0,
            section = mainSection
    )
    default String mainUsername() {
        return "";
    }

    @ConfigItem(
            keyName = "altUsername",
            name = "Alt Username",
            description = "Username of your alt account for identification",
            position = 0,
            section = altSection
    )
    default String altUsername() {
        return "";
    }

    @ConfigItem(
            keyName = "useDragonDarts",
            name = "Use Dragon Darts",
            description = "Use dragon darts instead of amethyst darts (more expensive but more lenient)",
            position = 1
    )
    default boolean useDragonDarts() {
        return false;
    }

    @ConfigItem(
            keyName = "useRingOfEndurance",
            name = "Use Ring of Endurance",
            description = "Enable if using Ring of Endurance for extended run energy",
            position = 2
    )
    default boolean useRingOfEndurance() {
        return false;
    }

    @ConfigItem(
            keyName = "drawOverlay",
            name = "Draw Overlay",
            description = "Draw overlay showing time remaining and fragments collected",
            position = 3
    )
    default boolean drawOverlay() {
        return true;
    }

    @ConfigItem(
            keyName = "altAutoRetaliate",
            name = "Alt Auto Retaliate",
            description = "Enable auto retaliate for alt account",
            position = 1,
            section = altSection
    )
    default boolean altAutoRetaliate() {
        return true;
    }
}