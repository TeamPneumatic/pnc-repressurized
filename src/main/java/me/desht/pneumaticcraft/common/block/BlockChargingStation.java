package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockChargingStation extends BlockPneumaticCraftCamo {
    public static final BooleanProperty CHARGE_PAD = BooleanProperty.create("charge_pad");

    private static final VoxelShape SHAPE = Block.makeCuboidShape(3, 0, 3, 13, 10, 13);

    public BlockChargingStation() {
        super(Material.IRON, "charging_station");
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(CHARGE_PAD);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return state.get(CHARGE_PAD) ? VoxelShapes.fullCube() : SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityChargingStation.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityChargingStation) {
            return ((TileEntityChargingStation) te).shouldEmitRedstone() ? 15 : 0;
        }
        return 0;
    }
}
