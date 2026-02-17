package com.bestiarymap;

import com.bestiarymap.util.datatypes.Monster;
import com.bestiarymap.util.MonsterData;
import com.bestiarymap.util.datatypes.Spawn;
import net.runelite.api.*;
import net.runelite.api.Menu;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.input.KeyListener;

import static com.bestiarymap.util.RenderHelper.*;
import com.bestiarymap.util.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class BestiaryMapOverlay extends Overlay {
    private final Client client;

    @Inject
    private BestiaryMapConfig config;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private WorldMapPointManager worldMapPointManager;

    @Inject
    private MonsterData monsterData;

    @Inject
    private ClientThread clientThread;

    private LabelBuilder groupNumLabel;
    private ButtonBuilder toggleOverlayButton, previousButton, nextButton;
    private InputBuilder searchBar;

    @Inject
    private MenuManager menuManager;

    private Boolean overlayEnabled = false;
    private Boolean searchFocused = false;

    private static final WidgetMenuOption BESTIARY_SHOW_OPTION = new WidgetMenuOption("Show", "Bestiary Overlay", InterfaceID.Worldmap.BOTTOM_GRAPHIC0);
    private static final WidgetMenuOption BESTIARY_HIDE_OPTION = new WidgetMenuOption("Hide", "Bestiary Overlay", InterfaceID.Worldmap.BOTTOM_GRAPHIC0);

    private enum MenuOptionState {NONE, SHOW, HIDE}

    private MenuOptionState menuOptionState = MenuOptionState.NONE;

    @Inject
    public BestiaryMapOverlay(Client client) {
        this.client = client;
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        // Button to toggle the world map bestiary overlay
        toggleOverlayButton = new ButtonBuilder()
            .SetSize(36, 24)
            .SetIcon(579)
            .SetTooltip("Bestiary Overlay")
            .SetAlignment(Alignment.RIGHT);

        // Search bar to filter monster names
        searchBar = new InputBuilder()
            .SetSize(200, 20)
            .SetPlaceholderLabel("Monster Search")
            .SetAlignment(Alignment.BOTTOM_RIGHT);

        // Button to jump to previous group of searched monsters
        previousButton = new ButtonBuilder()
            .SetSize(16, 16)
            .SetIcon(788)
            .SetIconRotation(90)
            .SetAlignment(Alignment.LEFT);

        // Button to jump to next group of searched monsters
        nextButton = new ButtonBuilder()
            .SetSize(16, 16)
            .SetIcon(788)
            .SetIconRotation(-90)
            .SetAlignment(Alignment.LEFT);

        // Label showing current focus and total groups
        groupNumLabel = new LabelBuilder()
            .SetAlignment(Alignment.BOTTOM)
            .SetFontStyle(FontStyle.SMALL)
            .SetColor(Color.YELLOW);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Widget worldmapBottomBarWidget = client.getWidget(InterfaceID.Worldmap.BOTTOM_GRAPHIC0);

        // Return early if the worldmap bottom bar widget is not available (which means the world map isn't open)
        if (worldmapBottomBarWidget == null)
            return null;

        Widget worldmapZoomOutWidget = client.getWidget(InterfaceID.Worldmap.ZOOM_OUT); // Map zoom out button 38993947

        if (worldmapZoomOutWidget == null)
            return null;

        AffineTransform originalTransform = graphics.getTransform();

        // Reset the transform origins so we draw on the full screen canvas and don't get pushed by other widgets
        graphics.setTransform(new AffineTransform());

        Rectangle zoomOutButtonBounds = worldmapZoomOutWidget.getBounds();
        Rectangle mapBottomBarBounds = worldmapBottomBarWidget.getBounds();

        Point mousePosition = client.getMouseCanvasPosition();

        toggleOverlayButton.SetPosition(zoomOutButtonBounds.x - 5, mapBottomBarBounds.y + (mapBottomBarBounds.height / 2));
        toggleOverlayButton.UpdateHoverState(mousePosition);

        if (toggleOverlayButton.isHovered) {
            if (menuOptionState == MenuOptionState.NONE) {
                if (overlayEnabled) {
                    // TODO: Add a hide bestiary menu option to right click menu
                    // menuManager.addManagedCustomMenu(BESTIARY_HIDE_OPTION, this::HideOverlay); // not working
                    menuOptionState = MenuOptionState.HIDE;
                } else {
                    // TODO: Add a show bestiary menu option to right click menu
                    // menuManager.addManagedCustomMenu(BESTIARY_SHOW_OPTION, this::ShowOverlay); // not working
                    menuOptionState = MenuOptionState.SHOW;
                }
            }
        } else if (menuOptionState != MenuOptionState.NONE) {
            // TODO: Clear the right click menu options
            // menuManager.removeManagedCustomMenu(menuOptionState == MenuOptionState.SHOW ? BESTIARY_SHOW_OPTION : BESTIARY_HIDE_OPTION); // not working
            menuOptionState = MenuOptionState.NONE;
        }

        toggleOverlayButton.Render(graphics, spriteManager, tooltipManager);

        if (overlayEnabled) {
            // Search bar user input field
            searchBar.SetPosition(toggleOverlayButton.getX() + toggleOverlayButton.getWidth() + 20, toggleOverlayButton.getY() - 15);
            searchBar.UpdateHoverState(mousePosition);
            searchBar.Render(graphics, spriteManager, tooltipManager);

            // Jump to previous monster group button
            previousButton.SetPosition(searchBar.getX() + searchBar.getWidth() + 6, searchBar.getY() + (searchBar.getHeight() / 2));
            previousButton.UpdateHoverState(mousePosition);
            previousButton.Render(graphics, spriteManager, tooltipManager);

            // Jump to next monster group button
            nextButton.SetPosition(previousButton.getX() + previousButton.getWidth() + 6, searchBar.getY() + (searchBar.getHeight() / 2));
            nextButton.UpdateHoverState(mousePosition);
            nextButton.Render(graphics, spriteManager, tooltipManager);

            // Label showing which monster group is focused out of how many total groups
            groupNumLabel.SetPosition(previousButton.getX() + previousButton.getWidth() + 3, searchBar.getY());
            groupNumLabel.Render(graphics);

            // TODO: Add find closest button if shortest path is installed?


            // Only render the following if it's visible within the worldMap rendered area
            Rectangle worldMapRectangle = client.getWidget(InterfaceID.Worldmap.MAP_CONTAINER).getBounds();
            Area worldMapClipArea =  GetWorldMapClipArea(client, worldMapRectangle);
            graphics.setClip(worldMapClipArea);


            WorldMap worldMap = client.getWorldMap();

            net.runelite.api.Point mapPoint = worldMap.getWorldMapPosition();

            int size = 8;

            // Pack the coordinate into the map zone plane
            int packedWorldPoint = PackWorldPoint(3150, 3500, 0);

            // Convert the packed coordinates to UI space x and y positions
            int xPos = MapWorldPointToGraphicsPointX(client, packedWorldPoint);//mapPoint.getX());
            int yPos = MapWorldPointToGraphicsPointY(client, packedWorldPoint);//mapPoint.getY());

            graphics.setColor(new Color(255, 0, 0, 150));
            graphics.fillRect(xPos, yPos, 100, 100);

            // TODO: Need to actually draw this within the map bounds
            // Draw a convex shape around each bestiary group
            //for (BestiaryGroup group : bestiaryGroups)
            //    drawOuterShape(client, graphics, group.spawnPoints);

            //WorldPoint testPoint = client.getLocalPlayer().getWorldLocation();

            //drawWorldMapSquare(graphics, client.getRenderOverview());
        }

        //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Map position: " + mapPosition.toString(), null);

        // Restore the screen transform
        graphics.setTransform(originalTransform);

        return null;
    }

    private List<WorldMapPoint> bestiaryPoints;
    private List<BestiaryGroup> bestiaryGroups;

    public void OnClick(MenuOptionClicked event) {
        // TODO: Find out what I need to do to get my widget recognised by MenuOptionClicked

        if (toggleOverlayButton.isHovered) {
            if (overlayEnabled) {
                HideOverlay(null);
            } else {
                ShowOverlay(null);
            }
        }

        if (overlayEnabled) {
            if (searchBar.isHovered) {
                // Set search focus when clicked
                SetSearchFocus(!searchFocused);
            } else if (searchFocused) {
                // Force lose search bar focus when clicking off it
                SetSearchFocus(false);
            } else if (previousButton.isHovered) {
                MapJumpPreviousGroup();
            } else if (nextButton.isHovered) {
                MapJumpNextGroup();
            }
        }
    }

    private void ShowOverlay(MenuEntry menuEntry) {
        overlayEnabled = true;
        toggleOverlayButton.SetToggledOn(true);

        GenerateBestiaryPoints();
    }

    private void HideOverlay(MenuEntry menuEntry) {
        overlayEnabled = false;
        toggleOverlayButton.SetToggledOn(false);

        ClearBestiaryPoints();
    }

    private void RefreshBestiaryPoints() {
        ClearBestiaryPoints();

        GenerateBestiaryPoints();
    }

    private class BestiaryGroup {
        WorldMapPoint centerPoint;
        ArrayList<WorldPoint> spawnPoints;

        public BestiaryGroup(WorldMapPoint centerPoint) {
            this.centerPoint = centerPoint;

            spawnPoints = new ArrayList<>();
            spawnPoints.add(centerPoint.getWorldPoint());
        }
    }

    private void GenerateBestiaryPoints() {
        bestiaryPoints = new ArrayList<WorldMapPoint>();
        bestiaryGroups = new ArrayList<BestiaryGroup>();

        // TODO: Change this to plane of active map opened rather than plane the player is physically in
        int activeMap = client.getLocalPlayer().getWorldLocation().getPlane();

        for (Monster monster : monsterData.getMonsters()) {
            if (monster.getSpawns().isEmpty())
                continue;

            if (!monster.getName().toLowerCase().contains(searchBar.getInputString().toLowerCase()))
                continue;

            for (Spawn spawn : monster.getSpawns()) {
                int mapId = spawn.getM();

                // TODO: Use checkbox for if we should only add spawns to current focused map
                // TODO: Find how to get active map open in the world map window
                //if(mapId != activeMap)
                //    continue;

                int x = spawn.getX();
                int y = spawn.getY();

                WorldMapPoint newMapPoint = new WorldMapPoint(new WorldPoint(x, y, mapId), DrawDot(config.spawnColor()));

                if(config.showIndividualSpawns()) {
                    newMapPoint.setName(monster.getName());
                    newMapPoint.setJumpOnClick(true);

                    bestiaryPoints.add(newMapPoint); // Add the point to the list so we can clean it up later

                    worldMapPointManager.add(newMapPoint); // Add the point to the worldMapPointManager to actually display it
                }

                AddToBestiaryGroupIfNotWithinRange(newMapPoint, 100);
            }
        }

        activeMapTarget = 0;
        groupNumLabel.SetText((activeMapTarget + 1) + " / " + bestiaryGroups.size());
    }

    private void AddToBestiaryGroupIfNotWithinRange(WorldMapPoint point, int range) {
        WorldPoint newPoint = point.getWorldPoint();

        for (BestiaryGroup existing : bestiaryGroups) {
            WorldPoint existingPoint = existing.centerPoint.getWorldPoint();

            // Already in range of an existing group, don't add this point
            if (existingPoint.distanceTo(newPoint) <= range) {
                // Add this spawn point into this bestiary group
                existing.spawnPoints.add(point.getWorldPoint());

                // TODO: Update the center point of the group to the new center
                //existing.centerPoint =

                return;
            }
        }

        bestiaryGroups.add(new BestiaryGroup(point));
    }


    private void ClearBestiaryPoints() {
        for (WorldMapPoint bestiaryPoint : bestiaryPoints) {
            if (bestiaryPoint != null)
                worldMapPointManager.remove(bestiaryPoint);
        }
    }

    // TODO: Add support for maps in range 10000 if possible?
    // TODO: Why are some spawns set to map id -1?
    private void SetWorldMapId(int mapId){
        Widget worldMapMapListItemWidget = client.getWidget(InterfaceID.Worldmap.MAPLIST_LIST); // 38993955 is the component id of the map dropdown menu list container
        Widget childWidget = worldMapMapListItemWidget.getChild(1); // "Gielinor Surface" child button

        // Get the event for the jump to map button (event 1711) - decompiled event: https://github.com/runelite/cs2-scripts/blob/c9ac2fcbc09899c9b28a8e36398e7114ba432e3d/scripts/%5Bclientscript%2Cworldmap_maplist_select%5D.cs2#L1
        Object[] worldMapJumpToMapEvent = childWidget.getOnOpListener();

        ScriptEvent jumpToMap = client.createScriptEvent(worldMapJumpToMapEvent);

        jumpToMap.getArguments()[1] = 1; // This was value "Integer.MIN_VALUE + 4" by default but there's an if check at the top of the function which instantly returns if it's not 1?
        jumpToMap.getArguments()[2] = mapId; // The map id to jump to
        jumpToMap.setSource(childWidget); // throws a warning if a source isn't set
        jumpToMap.run();

        // TODO: Remove this, it's just for debugging invalid maps
        if(mapId < 0 || mapId > 51){
            groupNumLabel.SetText(groupNumLabel.getText() + " <col=FF0000>INVALID MAP: " + mapId);
        }
    }

    private int activeMapTarget = 0;

    private void MapJumpPreviousGroup() {
        activeMapTarget = activeMapTarget - 1 < 0 ? bestiaryGroups.size() - 1 : activeMapTarget - 1;
        JumpToActiveGroup();
    }

    private void MapJumpNextGroup() {
        activeMapTarget = activeMapTarget + 1 >= bestiaryGroups.size() ? 0 : activeMapTarget + 1;
        JumpToActiveGroup();
    }

    private void JumpToActiveGroup(){
        groupNumLabel.SetText((activeMapTarget + 1) + " / " + bestiaryGroups.size());

        WorldMap worldMap = client.getWorldMap();
        WorldPoint worldPoint = bestiaryGroups.get(activeMapTarget).centerPoint.getWorldPoint();
        int targetMapId = worldPoint.getPlane();

        SetWorldMapId(targetMapId);
        worldMap.setWorldMapPositionTarget(worldPoint);
    }

    private void SetSearchFocus(boolean focused) {
        searchFocused = focused;
        searchBar.SetFocused(focused);

        // Set this variable while focused as key remapping plugins use this to temporarily disable remapping (this also disables chatbox input)
        // Must be invoked on the client thread, if focus was lost via a key event it isn't
        clientThread.invoke(() ->
        {
            client.setVarcIntValue(VarClientID.WORLDMAP_SEARCHING, focused ? 1 : 0);
        });
    }

    public final KeyListener keyListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (overlayEnabled && searchFocused) {
                char keyChar = e.getKeyChar();

                if (keyChar != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(keyChar)) {
                    searchBar.SetInputString(searchBar.getInputString() + keyChar);

                    RefreshBestiaryPoints();
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (overlayEnabled && searchFocused) {
                // Prevent the client listening to the key event (otherwise escape would close the world map rather than just ending focus)
                e.consume();

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    String current = searchBar.getInputString();

                    searchBar.SetInputString(current.length() > 0 ? current.substring(0, current.length() - 1) : "");
                    RefreshBestiaryPoints();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    SetSearchFocus(false);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) { }
    };
}
