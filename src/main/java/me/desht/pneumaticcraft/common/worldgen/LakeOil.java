package me.desht.pneumaticcraft.common.worldgen;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.stream.Stream;

public class LakeOil extends Placement<ChanceConfig> {
    public LakeOil(Codec<ChanceConfig> codec) {
        super(codec);
    }

//    LakeOil(Function<Dynamic<?>, ? extends ChanceConfig> configFactoryIn) {
//        super(configFactoryIn);
//    }

    @Override
    public Stream<BlockPos> getPositions(IWorld worldIn, ChunkGenerator generatorIn, Random random, ChanceConfig configIn, BlockPos pos) {
        if (!blacklisted(worldIn) && random.nextInt(100) < configIn.chance) {
            int i = random.nextInt(16);
            int j = random.nextInt(random.nextInt(generatorIn.func_230355_e_() - 8) + 8);  // getMaxHeight
            int k = random.nextInt(16);
            // oil lakes are fairly common underground, but a bit rarer on the surface
            if (j < worldIn.getSeaLevel() || random.nextInt(100) < 25) {
                return Stream.of(pos.add(i, j, k));
            }
        }
        return Stream.empty();
    }

    private boolean blacklisted(IWorld worldIn) {
        return PNCConfig.Common.General.oilWorldGenBlacklist.contains(worldIn.getWorld().func_234923_W_().func_240901_a_());
    }
}
