package site.s9lab.s9labclient.client.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class ResponsiveScreen extends Screen {
    private int lastWidth = -1;
    private int lastHeight = -1;

    protected ResponsiveScreen(Text title) {
        super(title);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        onResponsiveResize();
    }

    protected final void ensureResponsiveLayout() {
        if (lastWidth == this.width && lastHeight == this.height) {
            return;
        }
        lastWidth = this.width;
        lastHeight = this.height;
        onResponsiveResize();
    }

    protected void onResponsiveResize() {
    }

    protected ScreenLayout centeredLayout(int preferredWidth, int preferredHeight, int minWidth, int minHeight) {
        return ResponsiveLayout.centeredPanel(this.width, this.height, preferredWidth, preferredHeight, minWidth, minHeight);
    }

    protected static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
