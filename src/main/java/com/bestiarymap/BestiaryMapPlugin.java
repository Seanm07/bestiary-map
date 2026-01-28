package com.bestiarymap;

import com.bestiarymap.util.MonsterData;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import net.runelite.client.input.KeyManager;

@Slf4j
@PluginDescriptor(
        name = "Bestiary Map"
)
public class BestiaryMapPlugin extends Plugin {
    @Inject
    private BestiaryMapConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BestiaryMapOverlay overlay;

    @Inject
    private MonsterData monsterData;

    @Inject
    private KeyManager keyManager;

    @Override
    protected void startUp() throws Exception {
        monsterData.loadMonsterData();

        overlayManager.add(overlay);
        keyManager.registerKeyListener(overlay.keyListener);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        keyManager.unregisterKeyListener(overlay.keyListener);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Bestiary map plugin loaded " + config.greeting(), null);
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
