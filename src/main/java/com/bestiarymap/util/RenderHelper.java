package com.bestiarymap.util;

import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
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

    // TODO: Placeholder testing phase
    public static void drawOuterShape(Graphics2D g, ArrayList<WorldPoint> points) {
        if (points == null || points.size() < 3) {
            return; // Need at least 3 points to draw a polygon
        }

        // Convert WorldPoints to 2D points for convex hull calculation
        List<Point> pointList = new ArrayList<>();
        for (WorldPoint wp : points) {
            pointList.add(new Point(wp.getX(), wp.getY()));
        }

        // Compute convex hull using Graham Scan
        List<Point> hull = convexHull(pointList);

        // Create polygon path
        Path2D path = new Path2D.Double();
        Point first = hull.get(0);
        path.moveTo(first.x, first.y);
        for (int i = 1; i < hull.size(); i++) {
            Point p = hull.get(i);
            path.lineTo(p.x, p.y);
        }
        path.closePath();

        // Fill polygon with semi-transparent red
        Color fillColor = new Color(255, 0, 0, 150); // RGBA
        g.setColor(fillColor);
        g.fill(path);

        // Optional: draw border
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
