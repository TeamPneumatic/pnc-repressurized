package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LakesConfig;
import net.minecraft.world.gen.placement.LakeChanceConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class ModWorldGen {
    public static void init() {
        for (Biome biome: ForgeRegistries.BIOMES) {
            addOilLakes(biome);
        }
    }

    private static void addOilLakes(Biome biomeIn) {
        biomeIn.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, Biome.createDecoratedFeature(Feature.LAKE, new LakesConfig(ModBlocks.OIL.getDefaultState()), ModDecorators.OIL_LAKE.get(), new LakeChanceConfig(PNCConfig.Common.General.oilGenerationChance)));
    }
}
