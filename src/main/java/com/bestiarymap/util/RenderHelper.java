package com.bestiarymap.util;

import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class RenderHelper {
    private RenderHelper() {}

    public enum Alignment { TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT, TOP, MIDDLE }

    public static class LabelBuilder {
        private String text;
        private int x, y;
        private Color color = Color.BLACK;
        private Alignment alignment = Alignment.BOTTOM_LEFT;

        public LabelBuilder() {

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

        public void Render(Graphics2D graphics) {
            Rectangle rect = GetRenderAlignment(graphics.getFontMetrics());

            graphics.setColor(color);
            graphics.drawString(text, rect.x, rect.y);
        }

        private Rectangle GetRenderAlignment(FontMetrics font) {
            // drawn from bottom left (text baseline) by default

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
        private boolean isDirty;

        private int x, y, width, height, spriteId = -1;
        private Alignment alignment = Alignment.TOP_LEFT;

        private Rectangle buttonRect;
        public boolean isHovered, isToggledOn;

        private final int CORNER_SPRITE_SIZE = 9;
        private final int EDGE_SPRITE_SIZE = 3;
        private final int ICON_PADDING = 2;

        public ButtonBuilder() {

        }

        public ButtonBuilder SetPosition(int x, int y) {
            this.x = x;
            this.y = y;
            isDirty = true;
            return this;
        }

        public ButtonBuilder SetSize(int width, int height) {
            this.width = width;
            this.height = height;
            isDirty = true;
            return this;
        }

        public ButtonBuilder SetIcon(int spriteId){
            this.spriteId = spriteId;
            return this;
        }

        public ButtonBuilder SetAlignment(Alignment alignment) {
            this.alignment = alignment;
            isDirty = true;
            return this;
        }

        public void Render(Graphics2D graphics, SpriteManager spriteManager, TooltipManager tooltipManager) {
            // If the button has changed update the button bounds
            UpdateRenderAlignmentIfDirty();

            if(spriteManager != null) {
                // Button background
                BufferedImage bgSprite = spriteManager.getSprite(isHovered || isToggledOn ? 897 : 1040, 0);
                graphics.drawImage(bgSprite, buttonRect.x + 1, buttonRect.y + 1, buttonRect.width - 2, buttonRect.height - 2, null);

                // Inner shadow edges (left > top > right > bottom)
                for(int i=0;i < 4;i++){
                    Boolean leftOrRight = i == 0 || i == 2;

                    int curX = !leftOrRight ? buttonRect.x + EDGE_SPRITE_SIZE : (i == 0 ? buttonRect.x : buttonRect.x + buttonRect.width - EDGE_SPRITE_SIZE);
                    int curY = leftOrRight ? buttonRect.y + EDGE_SPRITE_SIZE : (i == 1 ? buttonRect.y : buttonRect.y + buttonRect.height - EDGE_SPRITE_SIZE);

                    BufferedImage innerEdgeSprite = spriteManager.getSprite((isHovered || isToggledOn ? 925 : 933) + i, 0);
                    graphics.drawImage(innerEdgeSprite, curX, curY, leftOrRight ? EDGE_SPRITE_SIZE : buttonRect.width - (EDGE_SPRITE_SIZE * 2), leftOrRight ? buttonRect.height - (EDGE_SPRITE_SIZE * 2) : EDGE_SPRITE_SIZE, null);
                }

                // Metal inner corners (top left > top right > bottom left > bottom right)
                for(int i=0;i < 4;i++){
                    int curX = i % 2 != 0 ? buttonRect.x + buttonRect.width - CORNER_SPRITE_SIZE : buttonRect.x;
                    int curY = i >= 2 ? buttonRect.y + buttonRect.height - CORNER_SPRITE_SIZE : buttonRect.y;

                    BufferedImage innerCornerSprite = spriteManager.getSprite((isHovered || isToggledOn ? 921 : 929) + i, 0);
                    graphics.drawImage(innerCornerSprite, curX, curY, CORNER_SPRITE_SIZE, CORNER_SPRITE_SIZE, null);
                }

                if(spriteId >= 0) {
                    // Button icon
                    BufferedImage sprite = spriteManager.getSprite(spriteId, 0);
                    int spriteAspectRatio = sprite.getHeight() / sprite.getWidth();
                    int spriteHeight = buttonRect.height - (EDGE_SPRITE_SIZE * 2);
                    int spriteWidth = spriteHeight * spriteAspectRatio;
                    graphics.drawImage(sprite, buttonRect.x + ((buttonRect.width - spriteWidth) / 2), buttonRect.y + ICON_PADDING, spriteWidth, spriteHeight, null);
                }
            } else {
                // Fallback to just drawing a red box
                graphics.setColor(Color.RED);
                graphics.drawRect(buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height);
            }

            if(isHovered && tooltipManager != null){
                tooltipManager.add(new Tooltip((isToggledOn ? "Hide" : "Show") + " <col=ff9040>Bestiary Overlay</col>"));
            }
        }

        public void UpdateHoverState(net.runelite.api.Point mouse){
            // If the button has changed update the button bounds
            UpdateRenderAlignmentIfDirty();

            isHovered = buttonRect != null && buttonRect.contains(mouse.getX(), mouse.getY());
        }

        public void SetToggledOn(Boolean toggledOn){
            isToggledOn = toggledOn;
        }

        private void UpdateRenderAlignmentIfDirty(){
            if(isDirty){
                buttonRect = GetRenderAlignment();
                isDirty = false;
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
