package com.bestiarymap;

import com.bestiarymap.util.Monster;
import com.bestiarymap.util.MonsterData;
import com.bestiarymap.util.Spawn;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import static com.bestiarymap.util.RenderHelper.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BestiaryMapOverlay extends Overlay {
    private final Client client;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private WorldMapPointManager worldMapPointManager;

    @Inject
    private MonsterData monsterData;

    private LabelBuilder coordinatesLabel;
    private ButtonBuilder toggleOverlayButton;
    private InputBuilder searchBar;

    private Boolean overlayEnabled = false;
    private Boolean searchFocused = false;

    @Inject
    public BestiaryMapOverlay(Client client) {
        this.client = client;
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        // Temporary coordinates label on world map to be removed later
        coordinatesLabel = new LabelBuilder()
            .SetColor(Color.WHITE)
            .SetAlignment(Alignment.RIGHT);

        // Button to toggle the world map bestiary overlay
        toggleOverlayButton = new ButtonBuilder()
            .SetSize(36, 24)
            .SetIcon(579)
            .SetAlignment(Alignment.RIGHT);

        // Search bar to filter monster names
        searchBar = new InputBuilder()
            .SetSize(200, 20)
            .SetPlaceholderLabel("Monster Search")
            .SetAlignment(Alignment.BOTTOM_RIGHT);
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
            if(worldmapBottomBarWidget == null) return null;

            Rectangle mapBottomBarBounds = worldmapBottomBarWidget.getBounds();

            Widget worldmapZoomOutWidget = client.getWidget(38993947); // Map zoom out button
            if(worldmapZoomOutWidget == null) return null;

            Rectangle zoomOutButtonBounds = worldmapZoomOutWidget.getBounds();

            toggleOverlayButton.SetPosition(zoomOutButtonBounds.x - 5, mapBottomBarBounds.y + (mapBottomBarBounds.height / 2));
            toggleOverlayButton.UpdateHoverState(mousePosition);
            toggleOverlayButton.Render(graphics, spriteManager, tooltipManager);

            if (overlayEnabled) {
                // TODO: Add search bar in a v bubble attached to bestiary overlay button
                searchBar.SetPosition(toggleOverlayButton.getX() + toggleOverlayButton.getWidth() + 20, toggleOverlayButton.getY() - 15);
                searchBar.UpdateHoverState(mousePosition);
                searchBar.Render(graphics, spriteManager, tooltipManager);

                // TODO: Add prev/next buttons attached to search bar

                // TODO: Add find closest button if shortest path is installed

                // TODO: To be removed later, just here for testing to make sure coordinates are correct atm
                coordinatesLabel.SetPosition(mapBottomBarBounds.x + mapBottomBarBounds.width - 185, mapBottomBarBounds.y + (mapBottomBarBounds.height / 2));
                coordinatesLabel.SetText(mapPos.getX() + ", " + mapPos.getY());
                coordinatesLabel.Render(graphics);
            }
        }

        //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Map position: " + mapPosition.toString(), null);

        // Restore the screen transform
        graphics.setTransform(originalTransform);

        return null;
    }

    // TODO: Move to RenderHelper (although current form is placeholder for testing)
    private BufferedImage createDot() {
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillOval(0, 0, 8, 8);
        g.dispose();
        return img;
    }

    private List<WorldMapPoint> bestiaryPoints;

    public void OnClick(MenuOptionClicked event) {
        // TODO: Find out what I need to do to get my widget recognised by MenuOptionClicked

        if (toggleOverlayButton.isHovered) {
            overlayEnabled = !overlayEnabled;
            toggleOverlayButton.SetToggledOn(overlayEnabled);

            if (overlayEnabled) {
                bestiaryPoints = new ArrayList<>();

                // TODO: Once bestiary search is implemented only show monsters matching search filter
                for (Monster monster : monsterData.getMonsters()) {
                    if (monster.getSpawns().isEmpty())
                        continue;

                    for (Spawn spawn : monster.getSpawns()) {
                        int mapId = spawn.getM();
                        int x = spawn.getX();
                        int y = spawn.getY();

                        // TODO: createDot() is temporary for testing
                        WorldMapPoint newMapPoint = new WorldMapPoint(new WorldPoint(x, y, mapId), createDot());

                        newMapPoint.setName(monster.getName());
                        newMapPoint.setJumpOnClick(true);

                        bestiaryPoints.add(newMapPoint); // Add the point to the list so we can clean it up later
                        worldMapPointManager.add(newMapPoint); // Add the point to the worldMapPointManager to actually display it
                    }
                }
            } else {
                for (WorldMapPoint bestiaryPoint : bestiaryPoints) {
                    if (bestiaryPoint != null)
                        worldMapPointManager.remove(bestiaryPoint);
                }
            }
        }

        if(overlayEnabled) {
            if (searchBar.isHovered) {
                searchFocused = !searchFocused;
                searchBar.SetFocused(searchFocused);
            }
        }
    }
}
