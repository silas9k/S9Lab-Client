package site.s9lab.s9labclient.client.module.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String value, String... modes) {
        super(name, value);
        this.modes = List.copyOf(Arrays.asList(modes));
    }

    public List<String> getModes() {
        return modes;
    }

    @Override
    public void setValue(String value) {
        if (modes.contains(value)) {
            super.setValue(value);
        }
    }
}
