package site.s9lab.s9labclient.client.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.font.TextRenderer;

public final class TextLayout {
    private TextLayout() {
    }

    public static String ellipsize(TextRenderer renderer, String text, int maxWidth) {
        if (text == null || maxWidth <= 0) {
            return "";
        }
        if (renderer.getWidth(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int suffixWidth = renderer.getWidth(suffix);
        String value = text;
        while (!value.isEmpty() && renderer.getWidth(value) + suffixWidth > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + suffix;
    }

    public static List<String> wrap(TextRenderer renderer, String text, int maxWidth, int maxLines) {
        ArrayList<String> lines = new ArrayList<>();
        if (text == null || text.isBlank() || maxLines <= 0) {
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (renderer.getWidth(candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
            current.setLength(0);
            current.append(word);
            if (lines.size() == maxLines - 1) {
                break;
            }
        }
        if (!current.isEmpty() && lines.size() < maxLines) {
            lines.add(ellipsize(renderer, current.toString(), maxWidth));
        }
        return lines;
    }
}
