package com.bestiarymap.util;

import lombok.Getter;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

public final class RenderHelper {
    private RenderHelper() {
    }

    public enum Alignment {TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT, TOP, MIDDLE}

    public enum FontStyle {NORMAL, SMALL, BOLD}

    public static class LabelBuilder {
        private String text, renderedText;
        @Getter
        private int x, y, maxWidth;
        @Getter
        private FontStyle fontStyle = FontStyle.NORMAL;
        private Color color = Color.BLACK;
        private Alignment alignment = Alignment.BOTTOM_LEFT;

        // Use the runelite text component so we get the advantages of the osrs font features such as markup tags
        private final TextComponent textComponent = new TextComponent();

        public LabelBuilder() {
        }

        public LabelBuilder SetText(String text) {
            this.text = text;
            renderedText = text;
            return this;
        }

        public LabelBuilder SetPosition(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public LabelBuilder SetFontStyle(FontStyle fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public LabelBuilder SetMaxWidth(int maxWidth){
            this.maxWidth = maxWidth;
            return this;
        }

        public LabelBuilder SetColor(Color color) {
            this.color = color;
            return this;
        }

        public LabelBuilder SetAlignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public void Render(Graphics2D graphics) {
            Font font = FontManager.getDefaultFont();

            switch (fontStyle) {
                case NORMAL:
                    font = FontManager.getRunescapeFont();
                    break;
                case SMALL:
                    font = FontManager.getRunescapeSmallFont();
                    break;
                case BOLD:
                    font = FontManager.getRunescapeBoldFont();
                    break;
            }

            graphics.setFont(font);

            FontMetrics fontMetrics = graphics.getFontMetrics();

            // If the font width is wider than max width trim the start of the string and replace with ellipsis
            if(maxWidth > 0 && fontMetrics.stringWidth(StripMarkup(renderedText)) > maxWidth){
                while(renderedText.length() > 3 && fontMetrics.stringWidth(StripMarkup(renderedText)) > maxWidth){
                    renderedText = renderedText.substring(1);
                }

                // Replace the first 2 characters with ellipsis
                renderedText = ".." + renderedText.substring(2);
            }

            Rectangle rect = GetRenderAlignment(fontMetrics);

            textComponent.setText(renderedText);
            textComponent.setPosition(new Point(rect.x, rect.y));
            textComponent.setFont(font);
            textComponent.setColor(color);

            textComponent.render(graphics);
        }

        private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]*>");

        public static String StripMarkup(String text) {
            return TAG_PATTERN.matcher(text).replaceAll("");
        }

        private Rectangle GetRenderAlignment(FontMetrics font) {
            // drawn from bottom left (text baseline) by default

            // x alignment adjustments
            if (alignment == Alignment.RIGHT || alignment == Alignment.TOP_RIGHT || alignment == Alignment.BOTTOM_RIGHT) {
                x -= font.stringWidth(StripMarkup(renderedText));
            } else if (alignment == Alignment.TOP || alignment == Alignment.MIDDLE || alignment == Alignment.BOTTOM) {
                x -= font.stringWidth(StripMarkup(renderedText)) / 2;
            }

            // y alignment adjustments
            if (alignment == Alignment.TOP_LEFT || alignment == Alignment.TOP || alignment == Alignment.TOP_RIGHT) {
                y += font.getAscent();
            } else if (alignment == Alignment.LEFT || alignment == Alignment.MIDDLE || alignment == Alignment.RIGHT) {
                y += font.getAscent() / 2;
            }

            return new Rectangle(x, y, font.stringWidth(StripMarkup(renderedText)), font.getHeight());
        }
    }

    public static class ButtonBuilder {
        private boolean isDirty;

        @Getter
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

        public ButtonBuilder SetIcon(int spriteId) {
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

            if (spriteManager != null) {
                // Button background
                BufferedImage bgSprite = spriteManager.getSprite(isHovered || isToggledOn ? 897 : 1040, 0);
                graphics.drawImage(bgSprite, buttonRect.x + 1, buttonRect.y + 1, buttonRect.width - 2, buttonRect.height - 2, null);

                // Inner shadow edges (left > top > right > bottom)
                for (int i = 0; i < 4; i++) {
                    Boolean leftOrRight = i == 0 || i == 2;

                    int curX = !leftOrRight ? buttonRect.x + EDGE_SPRITE_SIZE : (i == 0 ? buttonRect.x : buttonRect.x + buttonRect.width - EDGE_SPRITE_SIZE);
                    int curY = leftOrRight ? buttonRect.y + EDGE_SPRITE_SIZE : (i == 1 ? buttonRect.y : buttonRect.y + buttonRect.height - EDGE_SPRITE_SIZE);

                    BufferedImage innerEdgeSprite = spriteManager.getSprite((isHovered || isToggledOn ? 925 : 933) + i, 0);
                    graphics.drawImage(innerEdgeSprite, curX, curY, leftOrRight ? EDGE_SPRITE_SIZE : buttonRect.width - (EDGE_SPRITE_SIZE * 2), leftOrRight ? buttonRect.height - (EDGE_SPRITE_SIZE * 2) : EDGE_SPRITE_SIZE, null);
                }

                // Metal inner corners (top left > top right > bottom left > bottom right)
                for (int i = 0; i < 4; i++) {
                    int curX = i % 2 != 0 ? buttonRect.x + buttonRect.width - CORNER_SPRITE_SIZE : buttonRect.x;
                    int curY = i >= 2 ? buttonRect.y + buttonRect.height - CORNER_SPRITE_SIZE : buttonRect.y;

                    BufferedImage innerCornerSprite = spriteManager.getSprite((isHovered || isToggledOn ? 921 : 929) + i, 0);
                    graphics.drawImage(innerCornerSprite, curX, curY, CORNER_SPRITE_SIZE, CORNER_SPRITE_SIZE, null);
                }

                if (spriteId >= 0) {
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

            if (isHovered && tooltipManager != null) {
                tooltipManager.add(new Tooltip((isToggledOn ? "Hide" : "Show") + " <col=ff9040>Bestiary Overlay</col>"));
            }
        }

        public void UpdateHoverState(net.runelite.api.Point mouse) {
            // If the button has changed update the button bounds
            UpdateRenderAlignmentIfDirty();

            isHovered = buttonRect != null && buttonRect.contains(mouse.getX(), mouse.getY());
        }

        public void SetToggledOn(Boolean toggledOn) {
            isToggledOn = toggledOn;
        }

        private void UpdateRenderAlignmentIfDirty() {
            if (isDirty) {
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

    public static class InputBuilder {
        private boolean isDirty;

        @Getter
        private int x, y, width, height;
        private Alignment alignment = Alignment.TOP_LEFT;

        @Getter
        private String placeholderLabel, inputString = "";
        private Rectangle buttonRect;
        public boolean isHovered, isFocused;

        private LabelBuilder label = new LabelBuilder();

        public InputBuilder() {
            label.SetAlignment(Alignment.MIDDLE);
            label.SetFontStyle(FontStyle.SMALL);
        }

        public InputBuilder SetPosition(int x, int y) {
            this.x = x;
            this.y = y;
            isDirty = true;
            return this;
        }

        public InputBuilder SetSize(int width, int height) {
            this.width = width;
            this.height = height;
            isDirty = true;
            return this;
        }

        public InputBuilder SetAlignment(Alignment alignment) {
            this.alignment = alignment;
            isDirty = true;
            return this;
        }

        public InputBuilder SetPlaceholderLabel(String placeholderLabel) {
            this.placeholderLabel = placeholderLabel;
            return this;
        }

        public InputBuilder SetInputString(String inputString) {
            this.inputString = inputString;
            return this;
        }

        public void Render(Graphics2D graphics, SpriteManager spriteManager, TooltipManager tooltipManager) {
            // If the button has changed update the button bounds
            UpdateRenderAlignmentIfDirty();

            if (spriteManager != null) {
                // Down arrow sprite
                BufferedImage arrowSprite = spriteManager.getSprite(1000, 0);
                //graphics.drawImage(arrowSprite, buttonRect.x + 2, buttonRect.y + buttonRect.height, 20, 20, null);
                DrawRotatedSprite(graphics, arrowSprite, buttonRect.x + buttonRect.width - 48, buttonRect.y + (buttonRect.height / 2), 20, 20, -45);

                // Black border
                graphics.setColor(Color.BLACK);
                graphics.drawRect(buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height);

                // Inner gray border
                graphics.setColor(Color.decode("#474745"));
                graphics.drawRect(buttonRect.x + 1, buttonRect.y + 1, buttonRect.width - 2, buttonRect.height - 2);

                // Input background
                BufferedImage bgSprite = spriteManager.getSprite(isHovered || isFocused ? 297 : 897, 0);
                graphics.drawImage(bgSprite, buttonRect.x + 2, buttonRect.y + 2, buttonRect.width - 3, buttonRect.height - 3, null);

                // Draw label
                label.SetColor(Color.decode(isFocused ? "#ffffff" : "#9f9f9f"));
                label.SetText(inputString.isEmpty() && !isFocused ? placeholderLabel : inputString + (isFocused ? "<col=ff0000>*" : "*"));
                label.SetMaxWidth(buttonRect.width); // If the string is too long characters at the strip will be trimmed with ellipsis
                label.SetPosition(buttonRect.x + (buttonRect.width / 2), buttonRect.y + (buttonRect.height / 2));
                label.Render(graphics);
            } else {
                // Fallback to just drawing a red box
                graphics.setColor(Color.RED);
                graphics.drawRect(buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height);
            }

            if (isHovered && tooltipManager != null) {
                tooltipManager.add(new Tooltip((isFocused ? "Quit" : placeholderLabel)));
            }
        }

        public void UpdateHoverState(net.runelite.api.Point mouse) {
            // If the button has changed update the button bounds
            UpdateRenderAlignmentIfDirty();

            isHovered = buttonRect != null && buttonRect.contains(mouse.getX(), mouse.getY());
        }

        public void SetFocused(Boolean focused) {
            isFocused = focused;
        }

        private void UpdateRenderAlignmentIfDirty() {
            if (isDirty) {
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

    public static void DrawRotatedSprite(Graphics2D graphics, BufferedImage sprite, int x, int y, int width, int height, double angleDegrees) {
        AffineTransform transform = new AffineTransform();

        // Set origin to center of canvas
        transform.translate(x + width / 2, y + height / 2);

        // Rotate canvas around the center
        transform.rotate(Math.toRadians(angleDegrees));

        // Move origin back to top left of sprite
        transform.translate(-width / 2, -height / 2);

        // Draw the sprite with the new transform applied
        graphics.drawImage(sprite, transform, null);
    }

}
