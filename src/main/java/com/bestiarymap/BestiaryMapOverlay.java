package com.bestiarymap;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.SpritePixels;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;

import javax.inject.Inject;
import javax.swing.*;
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
        WorldMap worldMap = client.getWorldMap();

        // Only render map overlays once we have a valid world map ref
        if(worldMap != null) {
            Point mapPos = worldMap.getWorldMapPosition();

            Widget worldmapWidget = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
            Widget worldmapBottomBarWidget = client.getWidget(WidgetInfo.WORLD_MAP_BOTTOM_BAR);

            Rectangle bounds = worldmapWidget.getBounds();

            // Add coordinates to bottom bar below map
            RenderLabel(
                graphics,
                mapPos.getX() + ", " + mapPos.getY(),
                bounds.x + bounds.width - 175,
                bounds.y + bounds.height + (worldmapBottomBarWidget.getHeight() / 2),
                Color.WHITE,
                Alignment.RIGHT
            );

            // Add a button
            RenderButton(
                graphics,
                bounds.x + bounds.width - 135,
                bounds.y + bounds.height + (worldmapBottomBarWidget.getHeight() / 2),
                36,
                24,
                Alignment.RIGHT
            );
        }

        //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Map position: " + mapPosition.toString(), null);

        return null;
    }

    public enum Alignment { TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT, TOP, MIDDLE }

    // TODO: Builder Pattern?
    public void RenderLabel(Graphics2D graphics, String text, int x, int y, Color color, Alignment alignment){
        FontMetrics font = graphics.getFontMetrics();

        // Text is positioned top left of the rendered area by default, adjust position based on alignment

        // x alignment adjustments
        if(alignment == Alignment.RIGHT || alignment == Alignment.TOP_RIGHT || alignment == Alignment.BOTTOM_RIGHT) {
            x -= font.stringWidth(text);
        } else if(alignment == Alignment.TOP || alignment == Alignment.MIDDLE || alignment == Alignment.BOTTOM) {
            x -= font.stringWidth(text) / 2;
        }

        // y alignment adjustments
        if(alignment == Alignment.BOTTOM_LEFT || alignment == Alignment.BOTTOM || alignment == Alignment.BOTTOM_RIGHT){
            y -= font.getHeight() / 2;
        } else if(alignment == Alignment.LEFT || alignment == Alignment.MIDDLE || alignment == Alignment.RIGHT){
            y -= font.getHeight();
        }

        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }

    public void RenderButton(Graphics2D graphics, int x, int y, int width, int height, Alignment alignment){
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x, y, width, height);

        //SpritePixels sprite = client.gets
    }

    /*@Subscribe
    public void onMousePressed(MousePressed event)
    {
        if (buttonBounds.contains(event.getX(), event.getY()))
        {
            isOverlayEnabled = !isOverlayEnabled;
            event.consume(); // optional, prevents other handlers
        }
    }*/


    //@Schedule(period = 2, unit = ChronoUnit.SECONDS)
}
