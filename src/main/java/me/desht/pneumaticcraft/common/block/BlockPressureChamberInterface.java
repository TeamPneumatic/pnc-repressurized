package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberWall;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.EnumMap;

public class BlockPressureChamberInterface extends BlockPneumaticCraft implements IBlockPressureChamber {
    private static final EnumMap<Axis,VoxelShape> SHAPES = new EnumMap<>(Axis.class);
    private static final EnumMap<Direction,VoxelShape> DOORS = new EnumMap<>(Direction.class);
    static {
        SHAPES.put(Axis.Z, VoxelShapes.or(
                Block.makeCuboidShape(0, 12, 0, 16, 16, 16),
                Block.makeCuboidShape(0, 0, 0, 16, 4, 16),
                Block.makeCuboidShape(0, 4, 0, 4, 12, 16),
                Block.makeCuboidShape(12, 4, 0, 16, 12, 16),
                Block.makeCuboidShape(4, 4, 1, 12, 5, 2),
                Block.makeCuboidShape(11, 5, 1, 12, 11, 2),
                Block.makeCuboidShape(4, 11, 1, 12, 12, 2),
                Block.makeCuboidShape(4, 5, 1, 5, 11, 2),
                Block.makeCuboidShape(11, 5, 14, 12, 11, 15),
                Block.makeCuboidShape(4, 4, 14, 12, 5, 15),
                Block.makeCuboidShape(4, 5, 14, 5, 11, 15),
                Block.makeCuboidShape(4, 11, 14, 12, 12, 15)
        ));
        SHAPES.put(Axis.Y, VoxelShapeUtils.rotateX(SHAPES.get(Axis.Z), 90));
        SHAPES.put(Axis.X, VoxelShapeUtils.rotateY(SHAPES.get(Axis.Z), 90));

        DOORS.put(Direction.DOWN, Block.makeCuboidShape(3, 1, 3, 13, 2, 13));
        DOORS.put(Direction.UP, Block.makeCuboidShape(3, 14, 3, 13, 15, 13));
        DOORS.put(Direction.NORTH, Block.makeCuboidShape(3, 3, 1, 13, 13, 2));
        DOORS.put(Direction.SOUTH, Block.makeCuboidShape(3, 3, 14, 13, 13, 15));
        DOORS.put(Direction.WEST, Block.makeCuboidShape(1, 3, 3, 2, 13, 13));
        DOORS.put(Direction.EAST, Block.makeCuboidShape(14, 3, 3, 15, 13, 13));
    }

    public BlockPressureChamberInterface() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction dir = getRotation(state);
        VoxelShape main = SHAPES.get(dir.getAxis());

        return PneumaticCraftUtils.getTileEntityAt(worldIn, pos, TileEntityPressureChamberInterface.class).map(teI -> {
            if (teI.outputProgress < TileEntityPressureChamberInterface.MAX_PROGRESS) {
                return VoxelShapes.combineAndSimplify(main, DOORS.get(dir), IBooleanFunction.OR);
            } else if (teI.inputProgress < TileEntityPressureChamberInterface.MAX_PROGRESS) {
                return VoxelShapes.combineAndSimplify(main, DOORS.get(dir.getOpposite()), IBooleanFunction.OR);
            } else {
                return main;
            }
        }).orElse(main);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberInterface.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (!par1World.isRemote && TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos)) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((ServerPlayerEntity) par5EntityLiving);
        }
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !world.isRemote) {
            PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPressureChamberInterface.class)
                    .ifPresent(TileEntityPressureChamberWall::onBlockBreak);
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }
}
