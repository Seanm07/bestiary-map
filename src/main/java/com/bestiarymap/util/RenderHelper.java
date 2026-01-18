package com.bestiarymap.util;

import net.runelite.client.game.SpriteManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Date;

public final class RenderHelper {
    private RenderHelper() {}

    public enum Alignment { TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT, TOP, MIDDLE }

    public static class LabelBuilder {
        private Graphics2D graphics;
        private String text;
        private int x, y;
        private Color color = Color.BLACK;
        private Alignment alignment = Alignment.BOTTOM_LEFT;

        public LabelBuilder(Graphics2D graphics, String text, int x, int y) {
            this.graphics = graphics;
            this.text = text;
            this.x = x;
            this.y = y;
        }

        public LabelBuilder text(String text) {
            this.text = text;
            return this;
        }

        public LabelBuilder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public LabelBuilder color(Color color) {
            this.color = color;
            return this;
        }

        public LabelBuilder alignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public void Render() {
            Rectangle rect = GetRenderAlignment();
            graphics.setColor(color);
            graphics.drawString(text, rect.x, rect.y);
        }

        private Rectangle GetRenderAlignment() {
            // drawn from bottom left (text baseline) by default
            FontMetrics font = graphics.getFontMetrics();

            // x alignment adjustments
            if (alignment == Alignment.RIGHT || alignment == Alignment.TOP_RIGHT || alignment == Alignment.BOTTOM_RIGHT) {
                x -= font.stringWidth(text);
            } else if (alignment == Alignment.TOP || alignment == Alignment.MIDDLE || alignment == Alignment.BOTTOM) {
                x -= font.stringWidth(text) / 2;
            }

            // y alignment adjustments
            if (alignment == Alignment.TOP_LEFT || alignment == Alignment.TOP || alignment == Alignment.TOP_RIGHT) {
                y += font.getAscent();
            } else if (alignment == Alignment.LEFT || alignment == Alignment.MIDDLE || alignment == Alignment.RIGHT) {
                y += font.getAscent() / 2;
            }

            return new Rectangle(x, y, font.stringWidth(text), font.getHeight());
        }
    }

    public static class ButtonBuilder {
        private Graphics2D graphics;
        private SpriteManager spriteManager;
        private int x, y, width, height;
        private Alignment alignment = Alignment.TOP_LEFT;

        public ButtonBuilder(Graphics2D graphics, SpriteManager spriteManager, int x, int y, int width, int height) {
            this.graphics = graphics;
            this.spriteManager = spriteManager;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public ButtonBuilder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public ButtonBuilder size(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public ButtonBuilder alignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public void Render() {
            Rectangle buttonRect = GetRenderAlignment();

            graphics.setColor(Color.BLACK);
            graphics.drawRect(buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height);

            if(spriteManager != null) {
                // Button background
                BufferedImage bgSprite = spriteManager.getSprite(1040, 0);
                graphics.drawImage(bgSprite, x, y, width, height, null);

                int spriteId = (int) (new Date().getTime()/1000) % 10000;

                // Button icon
                BufferedImage sprite = spriteManager.getSprite(spriteId, 0);
                int spriteAspectRatio = sprite.getHeight() / sprite.getWidth();
                graphics.drawImage(sprite, x, y, height * spriteAspectRatio, height, null);

                // Draw the metal things in the corners of the buttons


                //SpritePixels sprite = client.gets
            }
        }

        private Rectangle GetRenderAlignment() {
            // drawn from top left by default

            // x alignment adjustments
            if (alignment == Alignment.RIGHT || alignment == Alignment.TOP_RIGHT || alignment == Alignment.BOTTOM_RIGHT) {
                x -= width;
            } else if (alignment == Alignment.TOP || alignment == Alignment.MIDDLE || alignment == Alignment.BOTTOM) {
                x -= width / 2;
            }

            // y alignment adjustments
            if (alignment == Alignment.BOTTOM_LEFT || alignment == Alignment.BOTTOM || alignment == Alignment.BOTTOM_RIGHT) {
                y -= height;
            } else if (alignment == Alignment.LEFT || alignment == Alignment.MIDDLE || alignment == Alignment.RIGHT) {
                y -= height / 2;
            }

            return new Rectangle(x, y, width, height);
        }
    }

}
