package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModDecorators;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandlerWorldGen {
    private ConfiguredFeature<?,?> oilLake;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBiomeLoading(BiomeLoadingEvent event) {
        if (oilLake == null) {
            oilLake = Feature.LAKE
                    .withConfiguration(new BlockStateFeatureConfig(ModBlocks.OIL.get().getDefaultState()))
                    .withPlacement(ModDecorators.OIL_LAKE.get().configure(new ChanceConfig(PNCConfig.Common.General.oilGenerationChance)));
        }

        if (!PNCConfig.Common.General.oilWorldGenBlacklist.contains(event.getName())) {
            event.getGeneration().withFeature(GenerationStage.Decoration.LAKES, oilLake);
        }
    }
}
