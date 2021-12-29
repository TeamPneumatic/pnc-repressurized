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

package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModDecorators;
import me.desht.pneumaticcraft.common.core.ModFeatures;
import me.desht.pneumaticcraft.common.util.WildcardedRLMatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModWorldGen {
    public static ConfiguredFeature<?,?> OIL_LAKES;

    static WildcardedRLMatcher dimensionMatcher = null;
    static WildcardedRLMatcher biomeMatcher = null;

    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_FEATURE;

        OIL_LAKES = ModFeatures.OIL_LAKE.get()
                .configured(new BlockStateFeatureConfig(ModBlocks.OIL.get().defaultBlockState()))
                .decorated(ModDecorators.OIL_LAKE.get().configured(new ChanceConfig(ConfigHelper.getOilLakeChance())));
        Registry.register(registry, RL("oil_lakes"), OIL_LAKES);
    }

    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!isBiomeBlacklisted(event.getName()) && !ConfigHelper.common().general.oilWorldGenCategoryBlacklist.get().contains(event.getCategory().getName())) {
            event.getGeneration().addFeature(GenerationStage.Decoration.LAKES, OIL_LAKES);
        }
    }

    public static void clearBlacklistCache() {
        dimensionMatcher = null;
        biomeMatcher = null;
    }

    static boolean isBiomeBlacklisted(ResourceLocation biomeName) {
        if (biomeMatcher == null) {
            biomeMatcher = new WildcardedRLMatcher(ConfigHelper.common().general.oilWorldGenBlacklist.get());
        }
        return biomeMatcher.test(biomeName);
    }

    static boolean isDimensionBlacklisted(ISeedReader level) {
        if (dimensionMatcher == null) {
            dimensionMatcher = new WildcardedRLMatcher(ConfigHelper.common().general.oilWorldGenDimensionBlacklist.get());
        }
        return dimensionMatcher.test(level.getLevel().dimension().getRegistryName());
    }
}
