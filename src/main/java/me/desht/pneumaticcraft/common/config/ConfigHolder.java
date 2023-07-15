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

package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.common.block.entity.AerialInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.worldgen.OilLakeFilter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHolder {
    static ClientConfig client;
    static CommonConfig common;
    private static ForgeConfigSpec configCommonSpec;
    private static ForgeConfigSpec configClientSpec;

    public static void init() {
        final Pair<ClientConfig, ForgeConfigSpec> spec1 = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        client = spec1.getLeft();
        configClientSpec = spec1.getRight();

        final Pair<CommonConfig, ForgeConfigSpec> spec2 = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        common = spec2.getLeft();
        configCommonSpec = spec2.getRight();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHolder.configCommonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHolder.configClientSpec);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigHolder::onConfigChanged);
    }

    private static void onConfigChanged(final ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == ConfigHolder.configClientSpec) {
            refreshClient();
        } else if (config.getSpec() == ConfigHolder.configCommonSpec) {
            refreshCommon();
        }
    }

    static void refreshClient() {
        ClientArmorRegistry.getInstance().refreshConfig();
    }

    static void refreshCommon() {
        OilLakeFilter.DimensionFilter.clearMatcherCaches();
        AerialInterfaceBlockEntity.clearDimensionBlacklist();
    }
}
