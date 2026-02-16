package com.bestiarymap.util;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.RenderOverview;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.worldmap.WorldMap;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class RenderHelper {
    private RenderHelper() {
    }

    public enum Alignment {TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT, TOP, MIDDLE}

    public enum FontStyle {NORMAL, SMALL, BOLD}

    public static void DrawRotatedSprite(Graphics2D graphics, BufferedImage sprite, int x, int y, int width, int height, double angleDegrees) {
        AffineTransform transform = new AffineTransform();

        // Set origin to center of canvas
        transform.translate(x + width / 2, y + height / 2);

        // Rotate canvas around the center
        transform.rotate(Math.toRadians(angleDegrees));

        // Move origin back to top left of sprite
        transform.translate(-width / 2, -height / 2);

        // Scale the size of the sprite
        transform.scale((double) width / sprite.getWidth(), (double) height / sprite.getHeight());

        // Draw the sprite with the new transform applied
        graphics.drawImage(sprite, transform, null);
    }

    // Placeholder function
    public static BufferedImage DrawDot(Color dotColor) {
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(dotColor);
        g.fillOval(0, 0, 8, 8);
        g.dispose();
        return img;
    }

    public static Area GetWorldMapClipArea(Client client, Rectangle baseRectangle) {
        final Widget overview = client.getWidget(InterfaceID.Worldmap.OVERVIEW_CONTAINER);
        final Widget surfaceSelector = client.getWidget(InterfaceID.Worldmap.MAPLIST_BOX_GRAPHIC0);

        Area clipArea = new Area(baseRectangle);

        // Don't draw over the map overview if it's open
        if (overview != null && !overview.isHidden())
            clipArea.subtract(new Area(overview.getBounds()));

        // Don't draw over the map surface selector dropdown if it's open
        if (surfaceSelector != null && !surfaceSelector.isHidden())
            clipArea.subtract(new Area(surfaceSelector.getBounds()));

        return clipArea;
    }

    public static int CalculateMapPoint(Client client, int pointX, int pointY) {
        WorldMap worldMap = client.getWorldMap();
        float zoom = worldMap.getWorldMapZoom();
        int mapPoint = PackWorldPoint(worldMap.getWorldMapPosition().getX(), worldMap.getWorldMapPosition().getY(), 0);
        int middleX = MapWorldPointToGraphicsPointX(client, mapPoint);
        int middleY = MapWorldPointToGraphicsPointY(client, mapPoint);

        if (pointX == Integer.MIN_VALUE || pointY == Integer.MIN_VALUE ||
            middleX == Integer.MIN_VALUE || middleY == Integer.MIN_VALUE) {
            return -1;
        }

        final int dx = (int) ((pointX - middleX) / zoom);
        final int dy = (int) ((-(pointY - middleY)) / zoom);

        return dxdy(mapPoint, dx, dy);
    }

    public static int PackWorldPoint(int x, int y, int plane) {
        return (x & 0x7FFF) | ((y & 0x7FFF) << 15) | ((plane & 0x3) << 30);
    }

    public static int dxdy(int packedPoint, int dx, int dy) {
        int x = UnpackWorldX(packedPoint);
        int y = UnpackWorldY(packedPoint);
        int z = UnpackWorldPlane(packedPoint);
        return PackWorldPoint(x + dx, y + dy, z);
    }

    public static int UnpackWorldX(int packedPoint) {
        return packedPoint & 0x7FFF;
    }

    public static int UnpackWorldY(int packedPoint) {
        return (packedPoint >> 15) & 0x7FFF;
    }

    public static int UnpackWorldPlane(int packedPoint) {
        return (packedPoint >> 30) & 0x3;
    }

    public static int MapWorldPointToGraphicsPointX(Client client, int packedWorldPoint) {
        WorldMap worldMap = client.getWorldMap();

        float pixelsPerTile = worldMap.getWorldMapZoom();

        Widget map = client.getWidget(InterfaceID.Worldmap.MAP_CONTAINER);
        if (map != null) {
            Rectangle worldMapRect = map.getBounds();

            int widthInTiles = (int) Math.ceil(worldMapRect.getWidth() / pixelsPerTile);

            net.runelite.api.Point worldMapPosition = worldMap.getWorldMapPosition();

            int xTileOffset = UnpackWorldX(packedWorldPoint) + widthInTiles / 2 - worldMapPosition.getX();

            int xGraphDiff = ((int) (xTileOffset * pixelsPerTile));
            xGraphDiff += pixelsPerTile - Math.ceil(pixelsPerTile / 2);
            xGraphDiff += (int) worldMapRect.getX();

            return xGraphDiff;
        }
        return Integer.MIN_VALUE;
    }

    public static int MapWorldPointToGraphicsPointY(Client client, int packedWorldPoint) {
        WorldMap worldMap = client.getWorldMap();

        float pixelsPerTile = worldMap.getWorldMapZoom();

        Widget map = client.getWidget(InterfaceID.Worldmap.MAP_CONTAINER);
        if (map != null) {
            Rectangle worldMapRect = map.getBounds();

            int heightInTiles = (int) Math.ceil(worldMapRect.getHeight() / pixelsPerTile);

            net.runelite.api.Point worldMapPosition = worldMap.getWorldMapPosition();

            int yTileMax = worldMapPosition.getY() - heightInTiles / 2;
            int yTileOffset = (yTileMax - UnpackWorldY(packedWorldPoint) - 1) * -1;

            int yGraphDiff = (int) (yTileOffset * pixelsPerTile);
            yGraphDiff -= pixelsPerTile - Math.ceil(pixelsPerTile / 2);
            yGraphDiff = worldMapRect.height - yGraphDiff;
            yGraphDiff += (int) worldMapRect.getY();

            return yGraphDiff;
        }
        return Integer.MIN_VALUE;
    }

    public static int GetWorldMapExtentWidth(Client client, Rectangle baseRectangle) {
        return (
            UnpackWorldX(
                CalculateMapPoint(client,
                    baseRectangle.x + baseRectangle.width,
                    baseRectangle.y + baseRectangle.height)) -
            UnpackWorldX(
                CalculateMapPoint(client,
                    baseRectangle.x,
                    baseRectangle.y)));
    }

    public static int GetWorldMapExtentHeight(Client client, Rectangle baseRectangle) {
        return (
            UnpackWorldY(
                CalculateMapPoint(
                        client,
                    baseRectangle.x,
                    baseRectangle.y)) -
            UnpackWorldY(
                CalculateMapPoint(client,
                    baseRectangle.x + baseRectangle.width,
                    baseRectangle.y + baseRectangle.height)));
    }

    public static void drawWorldMapSquare(Graphics2D graphics, RenderOverview worldMap)
    {
        net.runelite.api.Point mapPoint = worldMap.getWorldMapPosition();

        if (mapPoint == null)
            return;

        int size = 8;

        graphics.setColor(new Color(255, 0, 0, 150));
        graphics.fillRect(mapPoint.getX() - size / 2,
                          mapPoint.getY() - size / 2,
                          size,
                          size);

        graphics.setColor(Color.RED);
        graphics.drawRect(mapPoint.getX() - size / 2,
                          mapPoint.getY() - size / 2,
                          size,
                          size);
    }

    // TODO: Placeholder testing phase
    public static void drawOuterShape(Client client, Graphics2D g, ArrayList<WorldPoint> worldPoints) {
        if (worldPoints == null || worldPoints.size() < 3) {
            return; // Need at least 3 points to draw a polygon
        }

        List<Point> canvasPoints = new ArrayList<>();

        for (WorldPoint wp : worldPoints)
        {
            if (wp.getPlane() != client.getPlane())
                continue;

            LocalPoint local = LocalPoint.fromWorld(client, wp);
            if (local == null)
                continue;

            Polygon tilePoly = Perspective.getCanvasTilePoly(client, local);
            if (tilePoly == null)
                continue;

            Rectangle bounds = tilePoly.getBounds();
            canvasPoints.add(new Point(bounds.x + bounds.width / 2,
                                       bounds.y + bounds.height / 2));
        }

        if (canvasPoints.size() < 3)
            return;

        List<Point> hull = convexHull(canvasPoints);

        Path2D path = new Path2D.Double();
        Point first = hull.get(0);
        path.moveTo(first.x, first.y);

        for (int i = 1; i < hull.size(); i++)
        {
            Point p = hull.get(i);
            path.lineTo(p.x, p.y);
        }

        path.closePath();

        g.setColor(new Color(255, 0, 0, 150));
        g.fill(path);

        g.setColor(Color.RED);
        g.draw(path);
    }

    // TODO: Placeholder testing phase
    private static List<Point> convexHull(List<Point> points) {
        // Sort points by x, then y
        points.sort((p1, p2) -> (p1.x != p2.x) ? Integer.compare(p1.x, p2.x) : Integer.compare(p1.y, p2.y));

        List<Point> lower = new ArrayList<>();
        for (Point p : points) {
            while (lower.size() >= 2 && cross(lower.get(lower.size() - 2), lower.get(lower.size() - 1), p) <= 0) {
                lower.remove(lower.size() - 1);
            }
            lower.add(p);
        }

        List<Point> upper = new ArrayList<>();
        for (int i = points.size() - 1; i >= 0; i--) {
            Point p = points.get(i);
            while (upper.size() >= 2 && cross(upper.get(upper.size() - 2), upper.get(upper.size() - 1), p) <= 0) {
                upper.remove(upper.size() - 1);
            }
            upper.add(p);
        }

        // Remove last point of each list (repeated)
        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);

        // Concatenate lower and upper
        List<Point> hull = new ArrayList<>(lower);
        hull.addAll(upper);
        return hull;
    }

    // TODO: Placeholder testing phase
    private static int cross(Point O, Point A, Point B) {
        return (A.x - O.x) * (B.y - O.y) - (A.y - O.y) * (B.x - O.x);
    }

}
