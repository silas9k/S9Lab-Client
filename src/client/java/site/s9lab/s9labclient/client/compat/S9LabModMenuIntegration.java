package site.s9lab.s9labclient.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import site.s9lab.s9labclient.client.ui.S9LabClientScreen;

public class S9LabModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return S9LabClientScreen::new;
    }
}
