package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockChargingStation extends BlockPneumaticCraftCamo {
    public static final BooleanProperty CHARGE_PAD = BooleanProperty.create("charge_pad");

    private static final VoxelShape BASE = Block.makeCuboidShape(1, 0, 1, 15, 1, 15);
    private static final VoxelShape FRAME = Block.makeCuboidShape(4, 1, 4, 12, 6, 12);
    private static final VoxelShape PAD_FRAME = Block.makeCuboidShape(3, 1, 3, 13, 16, 13);
    private static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(BASE, FRAME, IBooleanFunction.OR);
    private static final VoxelShape PAD_SHAPE = VoxelShapes.combineAndSimplify(BASE, PAD_FRAME, IBooleanFunction.OR);

    public BlockChargingStation() {
        super(ModBlocks.defaultProps());
        setDefaultState(getStateContainer().getBaseState().with(CHARGE_PAD, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(CHARGE_PAD);
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return state.get(CHARGE_PAD) ? PAD_SHAPE : SHAPE;
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
        return PneumaticCraftUtils.getTileEntityAt(blockAccess, pos, TileEntityChargingStation.class)
                .map(teCS -> teCS.getRedstoneController().shouldEmit() ? 15 : 0).orElse(0);
    }
}
