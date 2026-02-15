package com.bestiarymap;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("bestiarymap")
public interface BestiaryMapConfig extends Config {
    @ConfigItem(
            keyName = "showIndividualSpawns",
            name = "Show Individual Spawn Dots",
            description = "Should each monster spawn be shown as a dot on the map overlap?"
    )
    default boolean showIndividualSpawns() {
        return true;
    }

    @ConfigItem(
            keyName = "spawnColor",
            name = "Spawn Dot Color",
            description = "Color of individual monster spawns on the map overlay"
    )
    default Color spawnColor() {
        return Color.red;
    }
}
