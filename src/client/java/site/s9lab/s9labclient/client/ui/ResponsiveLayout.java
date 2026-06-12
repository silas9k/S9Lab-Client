package site.s9lab.s9labclient.client.ui;

public final class ResponsiveLayout {
    public static final int MIN_TOUCH_HEIGHT = 22;
    public static final int DEFAULT_MARGIN = 12;
    public static final int SMALL_MARGIN = 8;
    public static final int TINY_MARGIN = 4;
    public static final int DEFAULT_GAP = 10;

    private ResponsiveLayout() {
    }

    public static int margin(int width, int height) {
        int shortest = Math.min(width, height);
        if (shortest < 240) {
            return 4;
        }
        if (shortest < 360) {
            return 8;
        }
        return 12;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int panelWidth(int screenWidth, int preferred, int min) {
        int margin = margin(screenWidth, 9999);
        int available = Math.max(1, screenWidth - margin * 2);
        return Math.min(preferred, Math.max(Math.min(min, available), available));
    }

    public static int panelHeight(int screenHeight, int preferred, int min) {
        int margin = margin(9999, screenHeight);
        int available = Math.max(1, screenHeight - margin * 2);
        return Math.min(preferred, Math.max(Math.min(min, available), available));
    }

    public static boolean compact(int width, int height) {
        return width < 560 || height < 340;
    }

    public static int columns(int width, int minCardWidth, int maxColumns) {
        return clamp(width / Math.max(1, minCardWidth), 1, Math.max(1, maxColumns));
    }

    public static ScreenLayout centeredPanel(int screenWidth, int screenHeight, int preferredWidth, int preferredHeight, int minWidth, int minHeight) {
        int margin = margin(screenWidth, screenHeight);
        int availableW = safeWidth(screenWidth);
        int availableH = safeHeight(screenHeight);
        int width = clamp(Math.min(preferredWidth, availableW), Math.min(minWidth, availableW), availableW);
        int height = clamp(Math.min(preferredHeight, availableH), Math.min(minHeight, availableH), availableH);
        int x = clamp((screenWidth - width) / 2, margin, Math.max(margin, screenWidth - margin - width));
        int y = clamp((screenHeight - height) / 2, margin, Math.max(margin, screenHeight - margin - height));
        int padding = adaptivePadding(width, height);
        return new ScreenLayout(x, y, width, height, padding, margin, compact(screenWidth, screenHeight));
    }

    public static int safeWidth(int screenWidth) {
        int margin = margin(screenWidth, 9999);
        return Math.max(1, screenWidth - margin * 2);
    }

    public static int safeHeight(int screenHeight) {
        int margin = margin(9999, screenHeight);
        return Math.max(1, screenHeight - margin * 2);
    }

    public static int adaptivePadding(int width, int height) {
        int shortest = Math.min(width, height);
        if (shortest < 230 || width < 360) {
            return 8;
        }
        if (shortest < 360 || width < 520) {
            return 12;
        }
        return 16;
    }

    public static int adaptiveGap(int width, int height) {
        int shortest = Math.min(width, height);
        if (shortest < 240) {
            return 5;
        }
        if (shortest < 360) {
            return 7;
        }
        return DEFAULT_GAP;
    }

    public static int buttonHeight(int screenHeight) {
        if (screenHeight < 240) {
            return MIN_TOUCH_HEIGHT;
        }
        if (screenHeight < 360) {
            return 24;
        }
        return 28;
    }

    public static int scroll(int current, double verticalAmount, int maxScroll) {
        int step = Math.max(16, buttonHeight(9999));
        return clamp(current - (int) Math.round(verticalAmount * step), 0, Math.max(0, maxScroll));
    }
}
