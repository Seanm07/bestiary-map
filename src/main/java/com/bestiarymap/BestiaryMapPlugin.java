package com.bestiarymap;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
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
import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;

@Slf4j
@PluginDescriptor(
	name = "Bestiary Map"
)
public class BestiaryMapPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BestiaryMapConfig config;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BestiaryMapOverlay overlay;


	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (examplePoint != null)
		{
			worldMapPointManager.remove(examplePoint);
		}

		overlayManager.remove(overlay);
	}

	private WorldMapPoint examplePoint;

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			WorldPoint location = new WorldPoint(3200, 3200, 0);

			examplePoint = new WorldMapPoint(
					location,
					createDot()
			);

			examplePoint.setName("Example text on the world map");
			examplePoint.setJumpOnClick(false);

			worldMapPointManager.add(examplePoint);

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Custom plugin loaded " + config.greeting(), null);
		}
	}

	@Provides
	BestiaryMapConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BestiaryMapConfig.class);
	}

	private BufferedImage createDot()
	{
		BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.RED);
		g.fillOval(0, 0, 8, 8);
		g.dispose();
		return img;
	}

}
