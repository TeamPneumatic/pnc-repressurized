package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.worldgen.OilLakeFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModWorldGenProvider {
    public static final ResourceKey<ConfiguredFeature<?, ?>> OIL_LAKE
            = ResourceKey.create(Registries.CONFIGURED_FEATURE, RL("oil_lake"));
    public static final ResourceKey<PlacedFeature> OIL_LAKE_SURFACE
            = ResourceKey.create(Registries.PLACED_FEATURE, RL("oil_lake_surface"));
    public static final ResourceKey<PlacedFeature> OIL_LAKE_UNDERGROUND
            = ResourceKey.create(Registries.PLACED_FEATURE, RL("oil_lake_underground"));
    public static final ResourceKey<BiomeModifier> OIL_LAKE_SURFACE_BM
            = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, RL("oil_lake_surface"));
    public static final ResourceKey<BiomeModifier> OIL_LAKE_UNDERGROUND_BM
            = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, RL("oil_lake_underground"));


    static class ConfiguredFeatures {
        public static void bootstrap(BootstapContext<ConfiguredFeature<?,?>> ctx) {
            FeatureUtils.register(ctx, OIL_LAKE, Feature.LAKE, new LakeFeature.Configuration(
                    BlockStateProvider.simple(ModBlocks.OIL.get().defaultBlockState()),
                    BlockStateProvider.simple(Blocks.AIR.defaultBlockState())
            ));
        }
    }

    static class PlacedFeatures {
        public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
            var configuredFeatures = ctx.lookup(Registries.CONFIGURED_FEATURE);
            var oilLakeCF = configuredFeatures.getOrThrow(OIL_LAKE);

            PlacementUtils.register(ctx, OIL_LAKE_SURFACE, oilLakeCF, List.of(
                    RarityFilter.onAverageOnceEvery(25),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                    BiomeFilter.biome(),
                    OilLakeFilter.oilLakeFilter()
            ));

            PlacementUtils.register(ctx, OIL_LAKE_UNDERGROUND, oilLakeCF, List.of(
                    RarityFilter.onAverageOnceEvery(6),
                    InSquarePlacement.spread(),
                    HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())),
                    EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.allOf(
                                    BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE),
                                    BlockPredicate.insideWorld(new BlockPos(0, -5, 0))
                            ), 32
                    ),
                    SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5),
                    BiomeFilter.biome(),
                    OilLakeFilter.oilLakeFilter()
            ));
        }
    }

    static class BiomeModifiers {
        public static void bootstrap(BootstapContext<BiomeModifier> ctx) {
            var placedFeatures = ctx.lookup(Registries.PLACED_FEATURE);
            var biomeReg = ctx.lookup(Registries.BIOME);

            ctx.register(OIL_LAKE_SURFACE_BM,
                    new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                            biomeReg.getOrThrow(PneumaticCraftTags.Biomes.OIL_LAKES_SURFACE),
                            HolderSet.direct(placedFeatures.getOrThrow(OIL_LAKE_SURFACE)),
                            GenerationStep.Decoration.LAKES
                    )
            );
            ctx.register(OIL_LAKE_UNDERGROUND_BM,
                    new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                            biomeReg.getOrThrow(PneumaticCraftTags.Biomes.OIL_LAKES_UNDERGROUND),
                            HolderSet.direct(placedFeatures.getOrThrow(OIL_LAKE_UNDERGROUND)),
                            GenerationStep.Decoration.LAKES
                    )
            );
        }
    }
}
