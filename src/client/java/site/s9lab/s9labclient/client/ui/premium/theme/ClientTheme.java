package site.s9lab.s9labclient.client.ui.premium.theme;

public final class ClientTheme {
    public static final int DEFAULT_ACCENT = 0xFF8B5CFF;
    public static final int DEFAULT_BACKGROUND = 0xD6000000;
    public static final int DEFAULT_PANEL = 0xE914161A;
    public static final int DEFAULT_PANEL_SOFT = 0xB610131C;
    public static final int DEFAULT_CARD = 0xB9141822;
    public static final int DEFAULT_CARD_HOVER = 0xD91A2030;
    public static final int DEFAULT_TEXT = 0xFFFFFFFF;
    public static final int DEFAULT_MUTED_TEXT = 0xFF9AA1B2;
    public static final int DEFAULT_BORDER = 0x662C3344;
    public static final int DEFAULT_RADIUS = 2;
    public static final int DEFAULT_GAP = 10;

    private int accentColor;
    private int backgroundColor;
    private int panelColor;
    private int panelSoftColor;
    private int cardColor;
    private int cardHoverColor;
    private int textColor;
    private int mutedTextColor;
    private int borderColor;
    private int radius;
    private int gap;
    private boolean blurBackground;
    private boolean darkOverlay;

    public ClientTheme() {
        this(DEFAULT_ACCENT);
    }

    public ClientTheme(int accentColor) {
        this.accentColor = opaque(accentColor);
        this.backgroundColor = DEFAULT_BACKGROUND;
        this.panelColor = DEFAULT_PANEL;
        this.panelSoftColor = DEFAULT_PANEL_SOFT;
        this.cardColor = DEFAULT_CARD;
        this.cardHoverColor = DEFAULT_CARD_HOVER;
        this.textColor = DEFAULT_TEXT;
        this.mutedTextColor = DEFAULT_MUTED_TEXT;
        this.borderColor = DEFAULT_BORDER;
        this.radius = DEFAULT_RADIUS;
        this.gap = DEFAULT_GAP;
        this.blurBackground = true;
        this.darkOverlay = true;
    }

    public int accentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = opaque(accentColor);
    }

    public int backgroundColor() {
        return backgroundColor;
    }

    public int panelColor() {
        return panelColor;
    }

    public int panelSoftColor() {
        return panelSoftColor;
    }

    public int cardColor() {
        return cardColor;
    }

    public int cardHoverColor() {
        return cardHoverColor;
    }

    public int textColor() {
        return textColor;
    }

    public int mutedTextColor() {
        return mutedTextColor;
    }

    public int borderColor() {
        return borderColor;
    }

    public int radius() {
        return radius;
    }

    public int gap() {
        return gap;
    }

    public boolean blurBackground() {
        return blurBackground;
    }

    public void setBlurBackground(boolean blurBackground) {
        this.blurBackground = blurBackground;
    }

    public boolean darkOverlay() {
        return darkOverlay;
    }

    public static int withAlpha(int color, int alpha) {
        return (clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }

    public static int mix(int from, int to, float progress) {
        float t = Math.max(0.0F, Math.min(1.0F, progress));
        int a = lerp((from >>> 24) & 255, (to >>> 24) & 255, t);
        int r = lerp((from >>> 16) & 255, (to >>> 16) & 255, t);
        int g = lerp((from >>> 8) & 255, (to >>> 8) & 255, t);
        int b = lerp(from & 255, to & 255, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int opaque(int color) {
        return 0xFF000000 | (color & 0x00FFFFFF);
    }

    private static int lerp(int from, int to, float progress) {
        return Math.round(from + (to - from) * progress);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
