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

public class ModWorldGen {
    public static Holder<PlacedFeature> OIL_LAKE_SURFACE;
    public static Holder<PlacedFeature> OIL_LAKE_UNDERGROUND;

    static WildcardedRLMatcher dimensionMatcher = null;
    static WildcardedRLMatcher biomeMatcher = null;

    /**
     * Called from FMLCommonSetupEvent
     */
    public static void registerConfiguredFeatures() {
        LakeFeature.Configuration oilLakeConfig = new LakeFeature.Configuration(
                BlockStateProvider.simple(ModBlocks.OIL.get().defaultBlockState()),
                BlockStateProvider.simple(Blocks.AIR)
        );

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
        if (!isBiomeBlacklisted(event.getName()) && !ConfigHelper.common().worldgen.oilWorldGenCategoryBlacklist.get().contains(event.getCategory().getName())) {
            event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, OIL_LAKE_SURFACE);
            event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, OIL_LAKE_UNDERGROUND);
        }
    }

    public static void clearBlacklistCache() {
        dimensionMatcher = null;
        biomeMatcher = null;
    }

    static boolean isBiomeBlacklisted(ResourceLocation biomeName) {
        if (biomeMatcher == null) {
            biomeMatcher = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenBlacklist.get());
        }
        return biomeMatcher.test(biomeName);
    }

    static boolean isDimensionBlacklisted(WorldGenLevel level) {
        if (dimensionMatcher == null) {
            dimensionMatcher = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenDimensionBlacklist.get());
        }
        return dimensionMatcher.test(level.getLevel().dimension().getRegistryName());
    }
}
