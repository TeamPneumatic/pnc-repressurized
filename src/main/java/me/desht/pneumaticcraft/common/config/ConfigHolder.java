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
import me.desht.pneumaticcraft.common.block.entity.utility.AerialInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.worldgen.OilLakeFilter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHolder {
    static ClientConfig client;
    static CommonConfig common;

    private static ModConfigSpec configCommonSpec;
    private static ModConfigSpec configClientSpec;

    public static void init(ModContainer container, IEventBus modBus) {
        final Pair<ClientConfig, ModConfigSpec> spec1 = new ModConfigSpec.Builder().configure(ClientConfig::new);
        client = spec1.getLeft();
        configClientSpec = spec1.getRight();

        final Pair<CommonConfig, ModConfigSpec> spec2 = new ModConfigSpec.Builder().configure(CommonConfig::new);
        common = spec2.getLeft();
        configCommonSpec = spec2.getRight();

        container.registerConfig(ModConfig.Type.COMMON, ConfigHolder.configCommonSpec);
        container.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.configClientSpec);

        modBus.addListener(ConfigHolder::onConfigChanged);
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
