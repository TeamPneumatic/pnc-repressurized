package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyDrill;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockAssemblyDrill extends BlockPneumaticCraftModeled {
    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            BBConstants.ASSEMBLY_ROBOT_MIN_POS, 0F, BBConstants.ASSEMBLY_ROBOT_MIN_POS,
            BBConstants.ASSEMBLY_ROBOT_MAX_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS_TOP, BBConstants.ASSEMBLY_ROBOT_MAX_POS
    );
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MIN_POS,
            BBConstants.ASSEMBLY_ROBOT_MAX_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS_TOP, BBConstants.ASSEMBLY_ROBOT_MAX_POS
    );

    BlockAssemblyDrill() {
        super(Material.IRON, "assembly_drill");
        setBlockBounds(BLOCK_BOUNDS);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyDrill.class;
    }
}
