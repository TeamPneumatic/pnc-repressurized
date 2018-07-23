package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGasLift extends BlockPneumaticCraftModeled {
    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(0, 0, 0, 1, 10 / 16F, 1);

    BlockGasLift() {
        super(Material.IRON, "gas_lift");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BLOCK_BOUNDS;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityGasLift.class;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this,
                BlockPneumaticCraft.UP, BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityGasLift) {
            for (int i = 0; i < 6; i++) {
                if (i == 0) continue;  // never connects on the DOWN face
                state = state.withProperty(BlockPneumaticCraft.CONNECTION_PROPERTIES[i], ((TileEntityGasLift) te).sidesConnected[i]);
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
    public EnumGuiId getGuiID() {
        return EnumGuiId.GAS_LIFT;
    }
}
