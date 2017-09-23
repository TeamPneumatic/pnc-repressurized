package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public abstract class BlockPneumaticCraftModeled extends BlockPneumaticCraft {

    protected BlockPneumaticCraftModeled(Material par2Material, String registryName) {
        super(par2Material, registryName);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
