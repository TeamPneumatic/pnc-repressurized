package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockKeroseneLampLight extends BlockAir {
    BlockKeroseneLampLight() {
        setRegistryName("kerosene_lamp_light");
        setUnlocalizedName("kerosene_lamp_light");
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 15;
    }
}
