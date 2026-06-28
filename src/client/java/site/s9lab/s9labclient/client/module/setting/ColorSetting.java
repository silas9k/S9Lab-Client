package site.s9lab.s9labclient.client.module.setting;

public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int argb) {
        super(name, argb);
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(value == null ? 0xFFFFFFFF : value);
    }
}
