package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPressureChamberGlass extends BlockPressureChamberWallBase {

    BlockPressureChamberGlass() {
        super("pressure_chamber_glass");
        setResistance(20000.f);
    }

    private boolean isGlass(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == this;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumFacing d = side.getOpposite();
        return !isGlass(world, pos.offset(d)) || !isGlass(world, pos);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

}
