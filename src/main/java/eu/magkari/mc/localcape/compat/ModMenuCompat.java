package eu.magkari.mc.localcape.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import eu.magkari.mc.localcape.Localcape;
import net.minecraft.util.Util;

import java.nio.file.Files;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            if (Files.exists(Localcape.BASE_DIR)) Util.getPlatform().openPath(Localcape.BASE_DIR);
            return screen;
        };
    }
}
