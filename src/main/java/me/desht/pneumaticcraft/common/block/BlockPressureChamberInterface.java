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

public class BlockPressureChamberInterface extends BlockPneumaticCraft implements IBlockPressureChamber {
    private static final VoxelShape[] SHAPES = new VoxelShape[3];
    private static final VoxelShape[] DOORS = new VoxelShape[6];
    static {
        SHAPES[Axis.Z.ordinal()] = VoxelShapes.or(
                Block.makeCuboidShape(0, 0, 0, 16, 3, 16),
                Block.makeCuboidShape(0, 13, 0, 16, 16, 16),
                Block.makeCuboidShape(0, 0, 0, 3, 16, 16),
                Block.makeCuboidShape(13, 0, 0, 16, 16, 16),
                Block.makeCuboidShape(3, 3, 0, 5, 5, 16),
                Block.makeCuboidShape(11, 3, 0, 13, 5, 16),
                Block.makeCuboidShape(3, 11, 0, 5, 13, 16),
                Block.makeCuboidShape(11, 11, 0, 13, 13, 16)
        );
        SHAPES[Axis.Y.ordinal()] = VoxelShapeUtils.rotateX(SHAPES[Axis.Z.ordinal()], 90);
        SHAPES[Axis.X.ordinal()] = VoxelShapeUtils.rotateY(SHAPES[Axis.Z.ordinal()], 90);

        DOORS[0] = Block.makeCuboidShape(3, 1, 3, 13, 2, 13);
        DOORS[1] = Block.makeCuboidShape(3, 14, 3, 13, 15, 13);
        DOORS[2] = Block.makeCuboidShape(3, 3, 1, 13, 13, 2);
        DOORS[3] = Block.makeCuboidShape(3, 3, 14, 13, 13, 15);
        DOORS[4] = Block.makeCuboidShape(1, 3, 3, 2, 13, 13);
        DOORS[5] = Block.makeCuboidShape(14, 3, 3, 15, 13, 13);
    }

    public BlockPressureChamberInterface() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction dir = getRotation(state);
        VoxelShape main = SHAPES[dir.getAxis().ordinal()];

        return PneumaticCraftUtils.getTileEntityAt(worldIn, pos, TileEntityPressureChamberInterface.class).map(teI -> {
            if (teI.outputProgress < TileEntityPressureChamberInterface.MAX_PROGRESS) {
                return VoxelShapes.combineAndSimplify(main, DOORS[dir.getIndex()], IBooleanFunction.OR);
            } else if (teI.inputProgress < TileEntityPressureChamberInterface.MAX_PROGRESS) {
                return VoxelShapes.combineAndSimplify(main, DOORS[dir.getOpposite().getIndex()], IBooleanFunction.OR);
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
