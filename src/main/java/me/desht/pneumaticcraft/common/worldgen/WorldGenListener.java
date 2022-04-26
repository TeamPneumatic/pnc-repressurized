package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModWorldGen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.List;

public class WorldGenListener {
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        List<String> whitelist = ConfigHelper.common().worldgen.oilWorldGenCategoryWhitelist.get();
        List<String> blacklist = ConfigHelper.common().worldgen.oilWorldGenCategoryBlacklist.get();

        boolean shouldAddLakes = !whitelist.isEmpty() ?
                WorldGenFiltering.isBiomeOK(event.getName()) && whitelist.contains(event.getCategory().getName()) :
                WorldGenFiltering.isBiomeOK(event.getName()) && !blacklist.contains(event.getCategory().getName());

        if (shouldAddLakes) {
            event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, ModWorldGen.OIL_LAKE_SURFACE.getHolder().get());
            event.getGeneration().addFeature(GenerationStep.Decoration.LAKES, ModWorldGen.OIL_LAKE_UNDERGROUND.getHolder().get());
        }
    }
}
