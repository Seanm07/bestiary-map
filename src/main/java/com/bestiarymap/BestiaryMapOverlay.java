package com.bestiarymap;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import static com.bestiarymap.util.RenderHelper.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class BestiaryMapOverlay extends Overlay {
    private final Client client;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private WorldMapPointManager worldMapPointManager;

    private Point mousePosition;

    private LabelBuilder coordinatesLabel;
    private ButtonBuilder toggleOverlayButton;

    private Boolean overlayEnabled = false;

    @Inject
    public BestiaryMapOverlay(Client client) {
        this.client = client;
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        // Add coordinates to bottom bar below map
        coordinatesLabel = new LabelBuilder()
            .color(Color.WHITE)
            .alignment(Alignment.RIGHT);

        // Add a button
        toggleOverlayButton = new ButtonBuilder()
            .SetSize(36,24)
            .SetIcon(579)
            .SetSize(36,24)
            .SetAlignment(Alignment.RIGHT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        AffineTransform originalTransform = graphics.getTransform();

        // Reset the transform origins so we draw on the full canvas and don't get pushed by other widgets
        graphics.setTransform(new AffineTransform());

        WorldMap worldMap = client.getWorldMap();

        Point mousePosition = client.getMouseCanvasPosition();

        // Only render map overlays once we have a valid world map ref
        if (worldMap != null) {
            Point mapPos = worldMap.getWorldMapPosition();

            Widget worldmapBottomBarWidget = client.getWidget(WidgetInfo.WORLD_MAP_BOTTOM_BAR);
            Rectangle mapBottomBarBounds = worldmapBottomBarWidget.getBounds();

            Widget worldmapZoomOutWidget = client.getWidget(38993947); // Map zoom out button
            Rectangle zoomOutButtonBounds = worldmapZoomOutWidget.getBounds();

            toggleOverlayButton.SetPosition(zoomOutButtonBounds.x - 5, mapBottomBarBounds.y + (mapBottomBarBounds.height / 2));
            toggleOverlayButton.UpdateHoverState(mousePosition);
            toggleOverlayButton.Render(graphics, spriteManager, tooltipManager);

            if(overlayEnabled) {
                coordinatesLabel.position(mapBottomBarBounds.x + mapBottomBarBounds.width - 185, mapBottomBarBounds.y + (mapBottomBarBounds.height / 2));
                coordinatesLabel.text(mapPos.getX() + ", " + mapPos.getY());
                coordinatesLabel.Render(graphics);


            }
        }

        //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Map position: " + mapPosition.toString(), null);

        // Restore the screen transform
        graphics.setTransform(originalTransform);

        return null;
    }

    // TODO: Move to RenderHelper
    private BufferedImage createDot() {
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillOval(0, 0, 8, 8);
        g.dispose();
        return img;
    }

    private WorldMapPoint[] bestiaryPoints;

    public void OnClick(MenuOptionClicked event){
        // TODO: Find out what I need to do to get my widget recognised by MenuOptionClicked

        if(toggleOverlayButton.isHovered){
            overlayEnabled = !overlayEnabled;
            toggleOverlayButton.SetToggledOn(overlayEnabled);

            if(overlayEnabled) {
                // TODO: This is currently just placeholder red dot drawing
                bestiaryPoints = new WorldMapPoint[10];

                for(int i=0;i < 10;i++) {
                    WorldPoint location = new WorldPoint(3200 + (i * 50), 3200, 0);

                    bestiaryPoints[i] = new WorldMapPoint(
                            location,
                            createDot()
                    );

                    bestiaryPoints[i].setName("Test red dot " + i);
                    bestiaryPoints[i].setJumpOnClick(true);

                    worldMapPointManager.add(bestiaryPoints[i]);
                }
            } else {
                for(WorldMapPoint bestiaryPoint : bestiaryPoints) {
                    if (bestiaryPoint != null) {
                        worldMapPointManager.remove(bestiaryPoint);
                    }
                }
            }
        }
    }

    //@Schedule(period = 2, unit = ChronoUnit.SECONDS)
}
