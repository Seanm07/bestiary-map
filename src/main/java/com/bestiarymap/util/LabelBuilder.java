package com.bestiarymap.util;

import lombok.Getter;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.TextComponent;

import static com.bestiarymap.util.RenderHelper.*;

import java.awt.*;
import java.util.regex.Pattern;

public class LabelBuilder {
    private boolean isDirty;

    // Raw x and y input before alignment transform
    private int inputX, inputY;

    private Rectangle labelRect;

    @Getter
    private String text, renderedText;
    @Getter
    private int x, y, maxWidth;
    @Getter
    private FontStyle fontStyle = FontStyle.NORMAL;
    private Color color = Color.BLACK;
    private Alignment alignment = Alignment.BOTTOM_LEFT;

    // Use the runelite text component so we get the advantages of the osrs font features such as markup tags
    private final net.runelite.client.ui.overlay.components.TextComponent textComponent = new TextComponent();

    public LabelBuilder() {
    }

    public LabelBuilder SetText(String text) {
        this.text = text;
        renderedText = text;
        isDirty = true;
        return this;
    }

    public LabelBuilder SetPosition(int x, int y) {
        this.inputX = x;
        this.inputY = y;
        isDirty = true;
        return this;
    }

    public LabelBuilder SetFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
        isDirty = true;
        return this;
    }

    public LabelBuilder SetMaxWidth(int maxWidth){
        this.maxWidth = maxWidth;
        isDirty = true;
        return this;
    }

    public LabelBuilder SetColor(Color color) {
        this.color = color;
        return this;
    }

    public LabelBuilder SetAlignment(Alignment alignment) {
        this.alignment = alignment;
        isDirty = true;
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

        if(isDirty) {
            labelRect = GetRenderAlignment(fontMetrics);
            isDirty = false;
        }

        textComponent.setText(renderedText);
        textComponent.setPosition(new Point(labelRect.x, labelRect.y));
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
        x = inputX;
        y = inputY;

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
