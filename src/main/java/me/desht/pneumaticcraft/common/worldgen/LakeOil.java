package me.desht.pneumaticcraft.common.worldgen;

import com.mojang.datafixers.Dynamic;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.placement.LakeChanceConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class LakeOil extends Placement<LakeChanceConfig> {
    LakeOil(Function<Dynamic<?>, ? extends LakeChanceConfig> configFactoryIn) {
        super(configFactoryIn);
    }

    @Override
    public Stream<BlockPos> getPositions(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generatorIn, Random random, LakeChanceConfig configIn, BlockPos pos) {
        if (!blacklisted(worldIn) && random.nextInt(100) < configIn.chance) {
            int i = random.nextInt(16);
            int j = random.nextInt(random.nextInt(generatorIn.getMaxHeight() - 8) + 8);
            int k = random.nextInt(16);
            // oil lakes are fairly common underground, but a bit rarer on the surface
            if (j < worldIn.getSeaLevel() || random.nextInt(100) < 25) {
                return Stream.of(pos.add(i, j, k));
            }
        }
        return Stream.empty();
    }

    private boolean blacklisted(IWorld worldIn) {
        return PNCConfig.Common.General.oilWorldGenBlacklist.contains(DimensionType.getKey(worldIn.getDimension().getType()));
    }
}
