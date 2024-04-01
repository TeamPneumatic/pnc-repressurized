/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.config.subconfig;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;

public class AuxConfigHandler {
    private static final LevelResource FOLDER = new LevelResource(Names.MOD_ID);

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
                    Log.error("Config file {} failed to create! Unexpected things can happen!", subConfig.getConfigFilename());
                    Log.error("Error detail: " + e.getMessage());
                } catch (ClassCastException e) {
                    Log.error("Config file {} appears to be invalid JSON! Unexpected things can happen!", subConfig.getConfigFilename());
                    Log.error("Error detail: " + e.getMessage());
                }
            }
        }
    }

    public static void postInit(IAuxConfig.Sidedness sidedness) {
        File defaultConfigDir = new File(FMLPaths.CONFIGDIR.get().toFile(), Names.MOD_ID);
        for (IAuxConfig subConfig : EXTERNAL_CONFIGS) {
            if (!subConfig.getSidedness().matches(sidedness)) continue;
            // world-specific configs are server only
            if (subConfig.useWorldSpecificDir() && sidedness != IAuxConfig.Sidedness.SERVER) continue;
            File subFolder = subConfig.useWorldSpecificDir() ? getWorldSpecificDir() : defaultConfigDir;
            if (subFolder.exists() || subFolder.mkdirs()) {
                File subFile = new File(subFolder, subConfig.getConfigFilename() + ".cfg");
                try {
                    subConfig.postInit(subFile);
                } catch (IOException e) {
                    Log.error("Config file {} failed to create! Unexpected things can happen!", subConfig.getConfigFilename());
                    Log.error("Error detail: " + e.getMessage());
                } catch (ClassCastException e) {
                    Log.error("Config file {} appears to be invalid JSON! Unexpected things can happen!", subConfig.getConfigFilename());
                    Log.error("Error detail: " + e.getMessage());
                }
            }
        }
    }

    private static File getWorldSpecificDir() {
        return ServerLifecycleHooks.getCurrentServer().getWorldPath(FOLDER).toFile();
    }

    public static void clearPerWorldConfigs() {
        for (IAuxConfig subConfig : EXTERNAL_CONFIGS) {
            if (subConfig.useWorldSpecificDir()) subConfig.clear();
        }
    }
}
