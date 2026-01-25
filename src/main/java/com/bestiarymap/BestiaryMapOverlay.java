package com.bestiarymap;

import com.bestiarymap.util.Monster;
import com.bestiarymap.util.MonsterData;
import com.bestiarymap.util.Spawn;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Inject
    private ClientThread clientThread;

    private LabelBuilder coordinatesLabel;
    private ButtonBuilder toggleOverlayButton;
    private InputBuilder searchBar;

    @Inject
    private MenuManager menuManager;

    private Boolean overlayEnabled = false;
    private Boolean searchFocused = false;

    private static final WidgetMenuOption BESTIARY_SHOW_OPTION = new WidgetMenuOption("Show", "Bestiary Overlay", WidgetInfo.WORLD_MAP_BOTTOM_BAR);
    private static final WidgetMenuOption BESTIARY_HIDE_OPTION = new WidgetMenuOption("Hide", "Bestiary Overlay", WidgetInfo.WORLD_MAP_BOTTOM_BAR);

    private enum MenuOptionState { NONE, SHOW, HIDE }
    private MenuOptionState menuOptionState = MenuOptionState.NONE;

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
            if (worldmapBottomBarWidget == null)
                return null;

            Rectangle mapBottomBarBounds = worldmapBottomBarWidget.getBounds();

            Widget worldmapZoomOutWidget = client.getWidget(38993947); // Map zoom out button
            if (worldmapZoomOutWidget == null)
                return null;

            Rectangle zoomOutButtonBounds = worldmapZoomOutWidget.getBounds();

            toggleOverlayButton.SetPosition(zoomOutButtonBounds.x - 5, mapBottomBarBounds.y + (mapBottomBarBounds.height / 2));
            toggleOverlayButton.UpdateHoverState(mousePosition);

            if(toggleOverlayButton.isHovered){
                if(menuOptionState == MenuOptionState.NONE) {
                    if(overlayEnabled){
                        System.out.println("Hide menu option added");

                        client.createMenuEntry(-1).setOption("hide bestiary menu").setType(MenuAction.RUNELITE).onClick(e -> HideOverlay(null));

                        menuManager.addManagedCustomMenu(BESTIARY_HIDE_OPTION, this::HideOverlay);
                        menuOptionState = MenuOptionState.HIDE;
                    } else {
                        System.out.println("Show menu option added");

                        menuManager.addManagedCustomMenu(BESTIARY_SHOW_OPTION, this::ShowOverlay);
                        menuOptionState = MenuOptionState.SHOW;
                    }
                }
            } else if(menuOptionState != MenuOptionState.NONE) {
                System.out.println("cleared menu options");

                if(menuOptionState == MenuOptionState.SHOW) {
                    menuManager.removeManagedCustomMenu(BESTIARY_SHOW_OPTION);
                } else if(menuOptionState == MenuOptionState.HIDE){
                    menuManager.removeManagedCustomMenu(BESTIARY_HIDE_OPTION);
                }

                menuOptionState = MenuOptionState.NONE;
            }

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
            if(overlayEnabled){
                HideOverlay(null);
            } else {
                ShowOverlay(null);
            }
        }

        if (overlayEnabled) {
            if (searchBar.isHovered) {
                // Set search focus when clicked
                SetSearchFocus(!searchFocused);
            } else if(searchFocused){
                // Force lose search bar focus when clicking off it
                SetSearchFocus(false);
            }
        }
    }

    private void ShowOverlay(MenuEntry menuEntry){
        overlayEnabled = true;
        toggleOverlayButton.SetToggledOn(true);

        GenerateBestiaryPoints();
    }

    private void HideOverlay(MenuEntry menuEntry){
        overlayEnabled = false;
        toggleOverlayButton.SetToggledOn(false);

        ClearBestiaryPoints();
    }

    private void RefreshBestiaryPoints(){
        ClearBestiaryPoints();

        GenerateBestiaryPoints();
    }

    private void GenerateBestiaryPoints(){
        bestiaryPoints = new ArrayList<>();

        // TODO: Once bestiary search is implemented only show monsters matching search filter
        for (Monster monster : monsterData.getMonsters()) {
            if (monster.getSpawns().isEmpty())
                continue;

            if(!monster.getName().toLowerCase().contains(searchBar.getInputString().toLowerCase()))
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
    }

    private void ClearBestiaryPoints(){
        for (WorldMapPoint bestiaryPoint : bestiaryPoints) {
            if (bestiaryPoint != null)
                worldMapPointManager.remove(bestiaryPoint);
        }
    }

    private void SetSearchFocus(boolean focused){
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

                    if (current.length() > 0) {
                        searchBar.SetInputString(current.substring(0, current.length() - 1));
                    } else {
                        searchBar.SetInputString("");
                    }

                    RefreshBestiaryPoints();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    SetSearchFocus(false);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
    };
}
