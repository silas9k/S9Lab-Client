package site.s9lab.s9labclient.client.ui;

public record ScreenLayout(
        int x,
        int y,
        int width,
        int height,
        int padding,
        int margin,
        boolean compact
) {
    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public int contentX() {
        return x + padding;
    }

    public int contentY() {
        return y + padding;
    }

    public int contentWidth() {
        return Math.max(1, width - padding * 2);
    }

    public int contentHeight() {
        return Math.max(1, height - padding * 2);
    }
}
