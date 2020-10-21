package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModDecorators;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ModWorldGen {
    public static ConfiguredFeature<?,?> OIL_LAKES;

    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_FEATURE;

        OIL_LAKES = Feature.LAKE
                .withConfiguration(new BlockStateFeatureConfig(ModBlocks.OIL.get().getDefaultState()))
                .withPlacement(ModDecorators.OIL_LAKE.get().configure(new ChanceConfig(ConfigHelper.getOilLakeChance())));
        Registry.register(registry, RL("oil_lakes"), OIL_LAKES);
    }

    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (!PNCConfig.Common.General.oilWorldGenBlacklist.contains(event.getName())) {
            event.getGeneration().withFeature(GenerationStage.Decoration.LAKES, OIL_LAKES);
        }
    }
}
