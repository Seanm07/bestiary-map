package com.bestiarymap.util;

import lombok.Getter;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import static com.bestiarymap.util.RenderHelper.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class InputBuilder {
    private boolean isDirty;

    // Raw x and y input before alignment transform
    private int inputX, inputY;

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
        this.inputX = x;
        this.inputY = y;
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

        x = inputX;
        y = inputY;

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
