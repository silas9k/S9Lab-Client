package site.s9lab.s9labclient.client.module.impl.utility;

import java.util.ArrayList;
import java.util.List;
import site.s9lab.s9labclient.client.module.Module;
import site.s9lab.s9labclient.client.module.ModuleCategory;
import site.s9lab.s9labclient.client.module.setting.BooleanSetting;
import site.s9lab.s9labclient.client.module.setting.ModeSetting;
import site.s9lab.s9labclient.client.util.S9TextEffects;

public class TablistBadgeModule extends Module {
    private static final String[] EFFECTS = buildEffects();
    private final BooleanSetting plusRainbowName;
    private final BooleanSetting plusAnimatedName;
    private final BooleanSetting plusNameEffectsEnabled;
    private final BooleanSetting showOtherPlayersNameEffects;
    private final ModeSetting plusEffect1;
    private final ModeSetting plusEffect2;
    private final ModeSetting plusEffect3;

    public TablistBadgeModule() {
        super("Tablist Badge", "Shows S9/S9C+ icons and synchronized player-name shader effects.", ModuleCategory.UTILITY, true);
        this.plusRainbowName = addSetting(new BooleanSetting("Plus Rainbow Name", true));
        this.plusAnimatedName = addSetting(new BooleanSetting("Plus Animated Name", true));
        this.plusNameEffectsEnabled = addSetting(new BooleanSetting("Plus Name Effects Enabled", true));
        this.showOtherPlayersNameEffects = addSetting(new BooleanSetting("Show Other Plus Name Effects", true));
        this.plusEffect1 = addSetting(new ModeSetting("Plus Effect 1", "rainbow", EFFECTS));
        this.plusEffect2 = addSetting(new ModeSetting("Plus Effect 2", "wave", EFFECTS));
        this.plusEffect3 = addSetting(new ModeSetting("Plus Effect 3", "none", EFFECTS));
    }

    private static String[] buildEffects() {
        List<String> ids = new ArrayList<>();
        ids.add("none");
        ids.addAll(S9TextEffects.EFFECT_IDS);
        return ids.toArray(String[]::new);
    }

    public boolean plusRainbowName() { return plusRainbowName.getValue(); }
    public boolean plusAnimatedName() { return plusAnimatedName.getValue(); }
    public boolean plusNameEffectsEnabled() { return plusNameEffectsEnabled.getValue(); }
    public boolean showOtherPlayersNameEffects() { return showOtherPlayersNameEffects.getValue(); }

    public void setPlusNameEffectsEnabled(boolean enabled) { plusNameEffectsEnabled.setValue(enabled); }
    public void setShowOtherPlayersNameEffects(boolean enabled) { showOtherPlayersNameEffects.setValue(enabled); }

    public List<String> plusNameEffects() {
        return S9TextEffects.normalize(List.of(plusEffect1.getValue(), plusEffect2.getValue(), plusEffect3.getValue()));
    }

    public boolean isEffectSelected(String effectId) {
        return plusNameEffects().contains(effectId);
    }

    public void toggleEffect(String effectId) {
        if (!S9TextEffects.EFFECT_IDS.contains(effectId)) return;
        List<String> selected = new ArrayList<>(plusNameEffects());
        if (selected.remove(effectId)) {
            setEffects(selected);
            return;
        }
        if (effectId.startsWith("color_")) {
            selected.removeIf(id -> id.startsWith("color_"));
        }
        if (selected.size() >= 3) {
            selected.remove(0);
        }
        selected.add(effectId);
        setEffects(selected);
    }

    public void clearEffects() { setEffects(List.of()); }

    private void setEffects(List<String> raw) {
        List<String> normalized = S9TextEffects.normalize(raw);
        plusEffect1.setValue(normalized.size() > 0 ? normalized.get(0) : "none");
        plusEffect2.setValue(normalized.size() > 1 ? normalized.get(1) : "none");
        plusEffect3.setValue(normalized.size() > 2 ? normalized.get(2) : "none");
    }
}
