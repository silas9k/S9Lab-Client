package site.s9lab.s9labclient.client.ui.premium.theme;

import site.s9lab.s9labclient.client.S9LabClientClient;

public final class ThemeManager {
    private static final ClientTheme THEME = new ClientTheme();

    private ThemeManager() {
    }

    public static ClientTheme theme() {
        return THEME;
    }

    public static void loadFromConfig() {
        if (S9LabClientClient.getConfigManager() == null) {
            return;
        }
        THEME.setAccentColor(S9LabClientClient.getConfigManager().getUiAccentColor());
        THEME.setBlurBackground(S9LabClientClient.getConfigManager().isUiBlurEnabled());
    }

    public static void setAccentColor(int color) {
        THEME.setAccentColor(color);
        if (S9LabClientClient.getConfigManager() != null) {
            S9LabClientClient.getConfigManager().setUiAccentColor(THEME.accentColor());
            S9LabClientClient.getConfigManager().save();
        }
    }

    public static void setBlurBackground(boolean enabled) {
        THEME.setBlurBackground(enabled);
        if (S9LabClientClient.getConfigManager() != null) {
            S9LabClientClient.getConfigManager().setUiBlurEnabled(enabled);
            S9LabClientClient.getConfigManager().save();
        }
    }

    public static String accentHex() {
        return String.format("#%06X", THEME.accentColor() & 0x00FFFFFF);
    }
}
