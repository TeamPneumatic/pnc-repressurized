package me.desht.pneumaticcraft.datagen;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.worldgen.OilLakeFilter;
import net.minecraft.core.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    private static final ResourceLocation OIL_LAKE_RL = RL("oil_lake");
    private static final ResourceLocation OIL_LAKE_SURFACE_RL = RL("oil_lake_surface");
    private static final ResourceLocation OIL_LAKE_UNDERGROUND_RL = RL("oil_lake_underground");

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        RegistryAccess registryAccess = RegistryAccess.builtinCopy();
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

        generator.addProvider(event.includeServer(), new ModRecipeProvider(generator));
        generator.addProvider(event.includeServer(), new ModLootTablesProvider(generator));
        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(generator, blockTagsProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModFluidTagsProvider(generator, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModBiomeTagsProvider(generator, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModEntityTypeTagsProvider(generator, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(generator, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModGLMProvider(generator));
        generator.addProvider(event.includeServer(), new ModPoiTypeTagsProvider(generator, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModStructureTagsProvider(generator, event.getExistingFileHelper()));

        oilLakeDatagen(event, generator, existingFileHelper, registryOps);
    }

    private static void oilLakeDatagen(GatherDataEvent event, DataGenerator generator, ExistingFileHelper existingFileHelper, RegistryOps<JsonElement> registryOps) {
        // oil lake configured feature
        ConfiguredFeature<?,?> oilLakeCF = new ConfiguredFeature<>(
                Feature.LAKE,
                new LakeFeature.Configuration(
                        BlockStateProvider.simple(ModBlocks.OIL.get().defaultBlockState()),
                        BlockStateProvider.simple(Blocks.AIR.defaultBlockState())
                )
        );
        generator.addProvider(event.includeServer(), JsonCodecProvider.forDatapackRegistry(
                generator, existingFileHelper, Names.MOD_ID, registryOps, Registry.CONFIGURED_FEATURE_REGISTRY, Map.of(
                        OIL_LAKE_RL, oilLakeCF
                )
        ));

        // surface & underground oil lake placed features
        var oilLakeKey = ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, OIL_LAKE_RL);
        var oilLakeHolder = registryOps.registry(Registry.CONFIGURED_FEATURE_REGISTRY).orElseThrow().getOrCreateHolderOrThrow(oilLakeKey);
        PlacedFeature oilLakeSurface = new PlacedFeature(oilLakeHolder, ImmutableList.of(
                RarityFilter.onAverageOnceEvery(25),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome(),
                OilLakeFilter.oilLakeFilter())
        );
        PlacedFeature oilLakeUnderground = new PlacedFeature(oilLakeHolder, ImmutableList.of(
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
                OilLakeFilter.oilLakeFilter())
        );
        generator.addProvider(event.includeServer(), JsonCodecProvider.forDatapackRegistry(
                generator, existingFileHelper, Names.MOD_ID, registryOps, Registry.PLACED_FEATURE_REGISTRY, Map.of(
                        OIL_LAKE_SURFACE_RL, oilLakeSurface,
                        OIL_LAKE_UNDERGROUND_RL, oilLakeUnderground
                )
        ));

        // biome modifiers
        ResourceKey<PlacedFeature> oilLakeSurfaceKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, OIL_LAKE_SURFACE_RL);
        BiomeModifier surfaceModifier = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                registryOps.registry(Registry.BIOME_REGISTRY).orElseThrow().getOrCreateTag(PneumaticCraftTags.Biomes.OIL_LAKES_SURFACE),
                HolderSet.direct(registryOps.registry(Registry.PLACED_FEATURE_REGISTRY).orElseThrow().getOrCreateHolderOrThrow(oilLakeSurfaceKey)),
                GenerationStep.Decoration.LAKES
        );
        ResourceKey<PlacedFeature> oilLakeUndergroundKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, OIL_LAKE_UNDERGROUND_RL);
        BiomeModifier undergroundModifier = new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                registryOps.registry(Registry.BIOME_REGISTRY).orElseThrow().getOrCreateTag(PneumaticCraftTags.Biomes.OIL_LAKES_UNDERGROUND),
                HolderSet.direct(registryOps.registry(Registry.PLACED_FEATURE_REGISTRY).orElseThrow().getOrCreateHolderOrThrow(oilLakeUndergroundKey)),
                GenerationStep.Decoration.LAKES
        );
        generator.addProvider(event.includeServer(), JsonCodecProvider.forDatapackRegistry(
                generator, existingFileHelper, Names.MOD_ID, registryOps, ForgeRegistries.Keys.BIOME_MODIFIERS, Map.of(
                        OIL_LAKE_SURFACE_RL, surfaceModifier,
                        OIL_LAKE_UNDERGROUND_RL, undergroundModifier
                )
        ));
    }
}
