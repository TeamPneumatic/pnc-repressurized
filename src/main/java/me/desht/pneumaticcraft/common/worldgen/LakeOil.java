package me.desht.pneumaticcraft.common.worldgen;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.stream.Stream;

public class LakeOil extends Placement<ChanceConfig> {
    public LakeOil(Codec<ChanceConfig> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(WorldDecoratingHelper helper, Random rand, ChanceConfig chanceConfig, BlockPos pos) {
        if (rand.nextInt(100) < chanceConfig.chance) {
            int x = rand.nextInt(16) + pos.getX();
            int z = rand.nextInt(16) + pos.getZ();
            int y = rand.nextInt(rand.nextInt(helper.getGenDepth() - 8) + 8);
            // if position is not below sea level, reduced random chance for surface lake
            if (y < helper.getSeaLevel() || rand.nextInt(100) < PNCConfig.Common.General.surfaceOilGenerationChance) {
                return Stream.of(new BlockPos(x, y, z));
            }
        }

        return Stream.empty();
    }
}
