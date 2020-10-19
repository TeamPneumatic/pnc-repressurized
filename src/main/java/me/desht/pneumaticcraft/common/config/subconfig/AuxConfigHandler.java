package me.desht.pneumaticcraft.common.config.subconfig;

import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class AuxConfigHandler {
    private static final FolderName FOLDER = new FolderName(Names.MOD_ID);

    private static final IAuxConfig[] EXTERNAL_CONFIGS = new IAuxConfig[] {
            AmadronPlayerOffers.INSTANCE,
            ProgWidgetConfig.INSTANCE,
            ArmorFeatureStatus.INSTANCE,
            ThirdPartyConfig.INSTANCE,
            MicromissileDefaults.INSTANCE,
            ArmorHUDLayout.INSTANCE
    };

    public static void preInit() {
        File defaultConfigDir = new File(FMLPaths.CONFIGDIR.get().toFile(), Names.MOD_ID);
        for (IAuxConfig subConfig : EXTERNAL_CONFIGS) {
            if (subConfig.useWorldSpecificDir()) continue;  // world-specific configs can't be handled in pre-init; there's no world yet
            if (defaultConfigDir.exists() || defaultConfigDir.mkdirs()) {
                File subFile = new File(defaultConfigDir, subConfig.getConfigFilename() + ".cfg");
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
        File defaultConfigDir = new File(FMLPaths.CONFIGDIR.get().toFile(), Names.MOD_ID);
        for (IAuxConfig subConfig : EXTERNAL_CONFIGS) {
            File subFolder = subConfig.useWorldSpecificDir() ? getWorldSpecificDir() : defaultConfigDir;
            if (subFolder.exists() || subFolder.mkdirs()) {
                File subFile = new File(subFolder, subConfig.getConfigFilename() + ".cfg");
                if (!subFile.exists() && subConfig.useWorldSpecificDir()) {
                    maybeMigrateFile(new File(defaultConfigDir, subConfig.getConfigFilename() + ".cfg"), subFile);
                }
                try {
                    subConfig.postInit(subFile);
                } catch (IOException e) {
                    Log.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    Log.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                    e.printStackTrace();
                }
            }
        }
    }

    private static void maybeMigrateFile(File oldFile, File newFile) {
        // on first time load with new per-world config system: check if we need to migrate
        // a config file from the general config dir to the world-specific dir
        try {
            if (oldFile.exists()) {
                FileUtils.moveFile(oldFile, newFile);
                Log.info("Migrated " + oldFile + " to " + newFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getWorldSpecificDir() {
        return ServerLifecycleHooks.getCurrentServer().func_240776_a_(FOLDER).toFile();
    }

    public static void clearPerWorldConfigs() {
        for (IAuxConfig subConfig : EXTERNAL_CONFIGS) {
            if (subConfig.useWorldSpecificDir()) subConfig.clear();
        }
    }
}
