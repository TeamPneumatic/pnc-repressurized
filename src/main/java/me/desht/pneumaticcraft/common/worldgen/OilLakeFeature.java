package me.desht.pneumaticcraft.common.worldgen;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.LakesFeature;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.config.PNCConfig.Common.General.oilWorldGenCategoryBlacklist;

/**
 * Extended vanilla lake feature to allow blacklisting by dimension ID
 */
public class OilLakeFeature extends LakesFeature {
    private static Set<String> blacklistedNamespaces;

    public OilLakeFeature() {
        super(BlockStateFeatureConfig.CODEC);
    }

    @Override
    public boolean place(ISeedReader level, ChunkGenerator generator, Random rand, BlockPos pos, BlockStateFeatureConfig config) {
        return !isBlacklisted(level) && super.place(level, generator, rand, pos, config);
    }

    private boolean isBlacklisted(ISeedReader level) {
        if (blacklistedNamespaces == null) {
            blacklistedNamespaces = oilWorldGenCategoryBlacklist.stream()
                    .filter(s -> s.endsWith(":*"))
                    .map(s -> (s.split(":"))[0])
                    .collect(Collectors.toSet());
        }

        ResourceLocation dimId = level.getLevel().dimension().getRegistryName();
        return oilWorldGenCategoryBlacklist.contains(dimId.toString())
                || blacklistedNamespaces.contains(dimId.getNamespace());
    }

    public static void clearBlacklistCache() {
        blacklistedNamespaces = null;
    }
}
