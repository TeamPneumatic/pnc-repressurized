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

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFeatures;
import me.desht.pneumaticcraft.common.util.WildcardedRLMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.List;

public class ModWorldGen {
    private static Holder<PlacedFeature> OIL_LAKE_SURFACE;
    private static Holder<PlacedFeature> OIL_LAKE_UNDERGROUND;

    private static WildcardedRLMatcher dimensionMatcherB = null;
    private static WildcardedRLMatcher dimensionMatcherW = null;
    private static WildcardedRLMatcher biomeMatcherB = null;
    private static WildcardedRLMatcher biomeMatcherW = null;

    /**
     * Called from FMLCommonSetupEvent
     */
    public static void registerConfiguredFeatures() {
        // why is LakeFeature deprecated by vanilla?

        //noinspection deprecation
        LakeFeature.Configuration oilLakeConfig = new LakeFeature.Configuration(
                BlockStateProvider.simple(ModBlocks.OIL.get().defaultBlockState()),
                BlockStateProvider.simple(Blocks.AIR)
        );

        //noinspection deprecation
        Holder<ConfiguredFeature<LakeFeature.Configuration,?>> oilLake
                = FeatureUtils.register(Names.MOD_ID + ":lake_oil_underground", ModFeatures.OIL_LAKE.get(), oilLakeConfig);

        OIL_LAKE_SURFACE = PlacementUtils.register(Names.MOD_ID + ":lake_oil_surface", oilLake,
                RarityFilter.onAverageOnceEvery(ConfigHelper.common().worldgen.surfaceOilLakeFrequency.get()),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );
        OIL_LAKE_UNDERGROUND = PlacementUtils.register(Names.MOD_ID + ":lake_oil_underground", oilLake,
                RarityFilter.onAverageOnceEvery(ConfigHelper.common().worldgen.undergroundOilLakeFrequency.get()),
                InSquarePlacement.spread(),
                HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.allOf(
                                BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE),
                                BlockPredicate.insideWorld(new BlockPos(0, -5, 0))
                        ), 32
                ),
                SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5),
                BiomeFilter.biome()
        );
    }

    public static void onBiomeLoading(BiomeLoadingEvent event) {
        List<String> whitelist = ConfigHelper.common().worldgen.oilWorldGenCategoryWhitelist.get();
        List<String> blacklist = ConfigHelper.common().worldgen.oilWorldGenCategoryBlacklist.get();

        boolean generate = !whitelist.isEmpty() ?
                isBiomeOK(event.getName()) && whitelist.contains(event.getCategory().getName()) :
                isBiomeOK(event.getName()) && !blacklist.contains(event.getCategory().getName());

        if (generate) {
            event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, OIL_LAKE_SURFACE);
            event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, OIL_LAKE_UNDERGROUND);
        }
    }

    public static void clearBlacklistCache() {
        dimensionMatcherB = null;
        dimensionMatcherW = null;
        biomeMatcherB = null;
        biomeMatcherW = null;
    }

    static boolean isBiomeOK(ResourceLocation biomeName) {
        if (biomeMatcherB == null) {
            biomeMatcherB = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenBlacklist.get());
        }
        if (biomeMatcherW == null) {
            biomeMatcherW = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenWhitelist.get());
        }
        // non-empty whitelist match OR no blacklist match
        return biomeMatcherW.isEmpty() ? !biomeMatcherB.test(biomeName) : biomeMatcherW.test(biomeName);
    }

    static boolean isDimensionOK(WorldGenLevel level) {
        if (dimensionMatcherB == null) {
            dimensionMatcherB = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenDimensionBlacklist.get());
        }
        if (dimensionMatcherW == null) {
            dimensionMatcherW = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenDimensionWhitelist.get());
        }
        // non-empty whitelist match OR no blacklist match
        ResourceLocation name = level.getLevel().dimension().location();
        return dimensionMatcherW.isEmpty() ? !dimensionMatcherB.test(name) : dimensionMatcherW.test(name);
    }
}
