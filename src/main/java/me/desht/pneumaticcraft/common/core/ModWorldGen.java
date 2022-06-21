package me.desht.pneumaticcraft.common.core;

public class ModWorldGen {
//    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
//            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Names.MOD_ID);
//
//    public static final RegistryObject<Codec<OilLakeBiomeModifier>> OIL_LAKE_BIOME_MODIFIER_CODEC = BIOME_MODIFIER_SERIALIZERS.register("oil_lake", () ->
//            RecordCodecBuilder.create(builder -> builder.group(
//                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(OilLakeBiomeModifier::biomes),
//                    PlacedFeature.CODEC.fieldOf("feature").forGetter(OilLakeBiomeModifier::feature)
//            ).apply(builder, OilLakeBiomeModifier::new)));

//    public static final DeferredRegister<ConfiguredFeature<?,?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, Names.MOD_ID);
//    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, Names.MOD_ID);
//    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Names.MOD_ID);
//
////    public static final RegistryObject<Feature<LakeFeature.Configuration>> OIL_LAKE = FEATURES.register("oil_lake", OilLakeFeature::new);
//
//    // TODO 1.19 make this simply "oil_lake"
//    public static final RegistryObject<ConfiguredFeature<?,?>> OIL_LAKE_CF = CONFIGURED_FEATURES.register("lake_oil_underground",
//            () -> new ConfiguredFeature<>(OIL_LAKE.get(), new LakeFeature.Configuration(
//                    BlockStateProvider.simple(ModBlocks.OIL.get().defaultBlockState()),
//                    BlockStateProvider.simple(Blocks.AIR)
//            ))
//    );
//
//    public static final RegistryObject<PlacedFeature> OIL_LAKE_SURFACE = PLACED_FEATURES.register("lake_oil_surface",
//            () -> new PlacedFeature(OIL_LAKE_CF.getHolder().orElseThrow(), ImmutableList.of(
//                    RarityFilter.onAverageOnceEvery(ConfigHelper.common().worldgen.surfaceOilLakeFrequency.get()),
//                    InSquarePlacement.spread(),
//                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
//                    BiomeFilter.biome())
//            )
//    );
//
//    public static final RegistryObject<PlacedFeature> OIL_LAKE_UNDERGROUND = PLACED_FEATURES.register("lake_oil_underground",
//            () -> new PlacedFeature(OIL_LAKE_CF.getHolder().orElseThrow(), ImmutableList.of(
//                    RarityFilter.onAverageOnceEvery(ConfigHelper.common().worldgen.undergroundOilLakeFrequency.get()),
//                    InSquarePlacement.spread(),
//                    HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())),
//                    EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.allOf(
//                                    BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE),
//                                    BlockPredicate.insideWorld(new BlockPos(0, -5, 0))
//                            ), 32
//                    ),
//                    SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5),
//                    BiomeFilter.biome())
//            )
//    );
}
