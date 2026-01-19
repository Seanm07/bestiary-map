package com.bestiarymap;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;

@Slf4j
@PluginDescriptor(
        name = "Bestiary Map"
)
public class BestiaryMapPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private BestiaryMapConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BestiaryMapOverlay overlay;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Custom plugin loaded " + config.greeting(), null);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        overlay.OnClick(event);
    }

    @Provides
    BestiaryMapConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BestiaryMapConfig.class);
    }

}
