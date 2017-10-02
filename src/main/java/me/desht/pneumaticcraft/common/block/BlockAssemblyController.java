package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockAssemblyController extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            0f, 0f, 0f,
            1f, BBConstants.ASSEMBLY_BASE_HEIGHT, 1f
    );
    private static final AxisAlignedBB LEG_BOUNDS = new AxisAlignedBB(
            7/16f, 2/16f, 7/16f,
            9/16f, 12/16f, 9/16f
    );

    BlockAssemblyController() {
        super(Material.IRON, "assembly_controller");
        setBlockBounds(BLOCK_BOUNDS);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this,
                BlockPressureTube.DOWN, BlockPressureTube.NORTH, BlockPressureTube.SOUTH, BlockPressureTube.WEST, BlockPressureTube.EAST);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityAssemblyController) {
            for (int i = 0; i < 6; i++) {
                if (i == 1) continue;  // never connects on the UP face
                state = state.withProperty(BlockPressureTube.CONNECTION_PROPERTIES[i], ((TileEntityAssemblyController) te).sidesConnected[i]);
            }
        }
        return state;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyController.class;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, BLOCK_BOUNDS);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, LEG_BOUNDS);
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.ASSEMBLY_CONTROLLER;
    }
}
