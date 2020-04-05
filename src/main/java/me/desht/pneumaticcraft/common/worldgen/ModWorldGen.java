package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class ModWorldGen {
    public static void init() {
        BlockState oil = ModBlocks.OIL.get().getDefaultState();
        for (Biome biome: ForgeRegistries.BIOMES) {
            addOilLakes(biome, oil);
        }
    }

    private static void addOilLakes(Biome biomeIn, BlockState state) {
        biomeIn.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Feature.LAKE
                        .withConfiguration(new BlockStateFeatureConfig(state))
                        .withPlacement(ModDecorators.OIL_LAKE.get()
                                .configure(new ChanceConfig(PNCConfig.Common.General.oilGenerationChance))));
    }
}
