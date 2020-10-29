package me.desht.pneumaticcraft.common.worldgen;

import com.mojang.serialization.Codec;
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
            int i = rand.nextInt(16) + pos.getX();
            int j = rand.nextInt(16) + pos.getZ();
            int k = rand.nextInt(rand.nextInt(helper.func_242891_a() - 8) + 8);
            if (k < helper.func_242895_b() || rand.nextInt(100) < 25) {
                return Stream.of(new BlockPos(i, k, j));
            }
        }

        return Stream.empty();
    }
}
