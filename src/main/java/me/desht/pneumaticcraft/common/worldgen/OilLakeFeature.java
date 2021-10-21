package me.desht.pneumaticcraft.common.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.LakesFeature;

import java.util.Random;

/**
 * Extended vanilla lake feature to allow blacklisting by dimension ID
 */
public class OilLakeFeature extends LakesFeature {
    public OilLakeFeature() {
        super(BlockStateFeatureConfig.CODEC);
    }

    @Override
    public boolean place(ISeedReader level, ChunkGenerator generator, Random rand, BlockPos pos, BlockStateFeatureConfig config) {
        return !ModWorldGen.isDimensionBlacklisted(level) && super.place(level, generator, rand, pos, config);
    }
}
