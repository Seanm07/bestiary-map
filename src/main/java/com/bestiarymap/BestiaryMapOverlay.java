package com.bestiarymap;

import com.bestiarymap.util.RenderHelper;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import static com.bestiarymap.util.RenderHelper.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class BestiaryMapOverlay extends Overlay {
    private final Client client;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    public BestiaryMapOverlay(Client client) {
        this.client = client;
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        AffineTransform originalTransform = graphics.getTransform();

        // Reset the transform origins so we draw on the full canvas and don't get pushed by other widgets
        graphics.setTransform(new AffineTransform());

        WorldMap worldMap = client.getWorldMap();

        // Only render map overlays once we have a valid world map ref
        if (worldMap != null) {

            Point mapPos = worldMap.getWorldMapPosition();

            //Widget worldmapWidget = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
            //Rectangle mapBounds = worldmapWidget.getBounds();

            Widget worldmapBottomBarWidget = client.getWidget(WidgetInfo.WORLD_MAP_BOTTOM_BAR);
            Rectangle mapBottomBarBounds = worldmapBottomBarWidget.getBounds();

            Widget worldmapZoomOutWidget = client.getWidget(38993947); // Map zoom out button
            Rectangle zoomOutButtonBounds = worldmapZoomOutWidget.getBounds();

            // Add coordinates to bottom bar below map
            new LabelBuilder(
                graphics,
                mapPos.getX() + ", " + mapPos.getY(),
                mapBottomBarBounds.x + mapBottomBarBounds.width - 185,
                mapBottomBarBounds.y + (mapBottomBarBounds.height / 2)
            ).color(Color.WHITE).alignment(Alignment.RIGHT).Render();

            // Add a button
            new ButtonBuilder(
                graphics,
                spriteManager,
                zoomOutButtonBounds.x - 5,
                mapBottomBarBounds.y + (mapBottomBarBounds.height / 2),
                36,
                24
            ).alignment(Alignment.RIGHT).Render();
        }

        //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Map position: " + mapPosition.toString(), null);

        // Restore the screen transform
        graphics.setTransform(originalTransform);

        return null;
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
