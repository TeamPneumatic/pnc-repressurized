package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.material.Material;

public abstract class BlockPneumaticCraftModeled extends BlockPneumaticCraft {

    protected BlockPneumaticCraftModeled(Material par2Material, String registryName) {
        super(par2Material, registryName);
    }

//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isFullCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
//        return BlockFaceShape.UNDEFINED;
//    }
}
