package com.bestiarymap;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;

import javax.inject.Inject;
import java.awt.*;

public class BestiaryMapOverlay extends Overlay
{
    private final Client client;

    @Inject
    public BestiaryMapOverlay(Client client)
    {
        this.client = client;
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        net.runelite.api.worldmap.WorldMap worldMap = client.getWorldMap();

        if (worldMap == null) return null;

        // Central position the world map is focused on
        net.runelite.api.Point mapPosition = client.getWorldMap().getWorldMapPosition();

        if (mapPosition == null) return null;

        //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Map position: " + mapPosition.toString(), null);

        Widget worldmapWidget = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
        Widget worldmapBottomBarWidget = client.getWidget(WidgetInfo.WORLD_MAP_BOTTOM_BAR);

        String text = mapPosition.getX() + ", " + mapPosition.getY();

        FontMetrics fm = graphics.getFontMetrics();

        Rectangle bounds = worldmapWidget.getBounds();
        int x = bounds.x + bounds.width - fm.stringWidth(text) - 135;
        int y = bounds.y + bounds.height - (fm.getHeight() / 2) + (worldmapBottomBarWidget.getHeight() / 2);

        graphics.setColor(Color.WHITE);
        graphics.drawString(text, x, y);


        return null;
    }

    //@Schedule(period = 2, unit = ChronoUnit.SECONDS)
}
