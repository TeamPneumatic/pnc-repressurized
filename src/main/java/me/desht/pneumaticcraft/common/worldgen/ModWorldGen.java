package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class ModWorldGen {
    public static void init() {
        BlockState oil = ModBlocks.OIL.get().getDefaultState();
        ConfiguredFeature<?, ?> feature = Features.func_243968_a(Names.MOD_ID + ":" + "oil_lake", Feature.LAKE
                .withConfiguration(new BlockStateFeatureConfig(oil))
                .withPlacement(ModDecorators.OIL_LAKE.get().configure(new ChanceConfig(PNCConfig.Common.General.oilGenerationChance))));

        for (Biome biome : ForgeRegistries.BIOMES) {
            if (!PNCConfig.Common.General.oilWorldGenBlacklist.contains(biome.getRegistryName())) {
                new BiomeModifier(biome).addFeature(GenerationStage.Decoration.LAKES, feature);
            }
        }
    }
}
