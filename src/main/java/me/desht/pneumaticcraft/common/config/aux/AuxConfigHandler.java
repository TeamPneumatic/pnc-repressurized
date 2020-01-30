package me.desht.pneumaticcraft.common.config.aux;

import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;

public class AuxConfigHandler {
    private static final IAuxConfig[] EXTERNAL_CONFIGS = new IAuxConfig[] {
            AmadronPlayerOffers.INSTANCE,
            ProgWidgetConfig.INSTANCE,
            ArmorFeatureStatus.INSTANCE,
            ThirdPartyConfig.INSTANCE,
            MicromissileDefaults.INSTANCE,
            ArmorHUDLayout.INSTANCE
    };

    public static void preInit() {
        File configDir = FMLPaths.CONFIGDIR.get().toFile();
        for (IAuxConfig subConfig : EXTERNAL_CONFIGS) {
            File subFolder = new File(configDir, Names.MOD_ID);
            if (subFolder.exists() || subFolder.mkdirs()) {
                File subFile = new File(subFolder, subConfig.getConfigFilename() + ".cfg");
                try {
                    subConfig.preInit(subFile);
                } catch(IOException e) {
                    Log.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    Log.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void postInit() {
        for(IAuxConfig subConfig : EXTERNAL_CONFIGS) {
            try {
                subConfig.postInit();
            } catch(IOException e) {
                Log.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                e.printStackTrace();
            } catch (ClassCastException e) {
                Log.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                e.printStackTrace();
            }
        }
    }

}
