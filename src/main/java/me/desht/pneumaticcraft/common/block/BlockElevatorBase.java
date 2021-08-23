package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockElevatorBase extends BlockPneumaticCraftCamo {
    private static final VoxelShape BASE = box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape TOP  = box(0, 15, 0, 16, 16, 16);
    private static final VoxelShape CORE = box(3, 1, 3, 13, 15, 13);
    private static final VoxelShape SHAPE = VoxelShapes.or(BASE, CORE, TOP);

    public BlockElevatorBase() {
        super(ModBlocks.defaultProps().noOcclusion());  // notSolid() because of camo requirements
        registerDefaultState(getStateDefinition().any()
                .setValue(BlockPneumaticCraft.NORTH, false)
                .setValue(BlockPneumaticCraft.SOUTH, false)
                .setValue(BlockPneumaticCraft.WEST, false)
                .setValue(BlockPneumaticCraft.EAST, false)
        );
    }

    @Override
    public void onPlace(BlockState newState, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(newState, world, pos, oldState, isMoving);
        TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorBase.class;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return SHAPE;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        return super.use(state, world, getCoreElevatorPos(world, pos), player, hand, brtr);
    }

    private static BlockPos getCoreElevatorPos(World world, BlockPos pos) {
        if (world.getBlockState(pos.relative(Direction.UP)).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
            return getCoreElevatorPos(world, pos.relative(Direction.UP));
        } else {
            return pos;
        }
    }

    public static TileEntityElevatorBase getCoreTileEntity(World world, BlockPos pos) {
        return (TileEntityElevatorBase) world.getBlockEntity(getCoreElevatorPos(world, pos));
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockState(pos.relative(Direction.DOWN)).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
                TileEntity te = world.getBlockEntity(pos.relative(Direction.DOWN));
                ((TileEntityElevatorBase) te).moveUpgradesFromAbove();
            }
            TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, pos);
            if (elevatorBase != null) {
                elevatorBase.updateMaxElevatorHeight();
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}
