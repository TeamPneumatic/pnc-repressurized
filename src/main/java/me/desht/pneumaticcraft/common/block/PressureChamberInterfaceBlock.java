package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberValveBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberWallBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class PressureChamberInterfaceBlock extends AbstractPneumaticCraftBlock
        implements IBlockPressureChamber, PneumaticCraftEntityBlock
{
    private static final EnumMap<Axis,VoxelShape> SHAPES = new EnumMap<>(Axis.class);
    private static final EnumMap<Direction,VoxelShape> DOORS = new EnumMap<>(Direction.class);
    static {
        SHAPES.put(Axis.Z, Shapes.or(
                Block.box(0, 12, 0, 16, 16, 16),
                Block.box(0, 0, 0, 16, 4, 16),
                Block.box(0, 4, 0, 4, 12, 16),
                Block.box(12, 4, 0, 16, 12, 16),
                Block.box(4, 4, 1, 12, 5, 2),
                Block.box(11, 5, 1, 12, 11, 2),
                Block.box(4, 11, 1, 12, 12, 2),
                Block.box(4, 5, 1, 5, 11, 2),
                Block.box(11, 5, 14, 12, 11, 15),
                Block.box(4, 4, 14, 12, 5, 15),
                Block.box(4, 5, 14, 5, 11, 15),
                Block.box(4, 11, 14, 12, 12, 15)
        ));
        SHAPES.put(Axis.Y, VoxelShapeUtils.rotateX(SHAPES.get(Axis.Z), 90));
        SHAPES.put(Axis.X, VoxelShapeUtils.rotateY(SHAPES.get(Axis.Z), 90));

        DOORS.put(Direction.DOWN, Block.box(3, 1, 3, 13, 2, 13));
        DOORS.put(Direction.UP, Block.box(3, 14, 3, 13, 15, 13));
        DOORS.put(Direction.NORTH, Block.box(3, 3, 1, 13, 13, 2));
        DOORS.put(Direction.SOUTH, Block.box(3, 3, 14, 13, 13, 15));
        DOORS.put(Direction.WEST, Block.box(1, 3, 3, 2, 13, 13));
        DOORS.put(Direction.EAST, Block.box(14, 3, 3, 15, 13, 13));
    }

    public PressureChamberInterfaceBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction dir = getRotation(state);
        VoxelShape main = SHAPES.get(dir.getAxis());

        return worldIn.getBlockEntity(pos, ModBlockEntityTypes.PRESSURE_CHAMBER_INTERFACE.get()).map(teI -> {
            if (teI.outputProgress < PressureChamberInterfaceBlockEntity.MAX_PROGRESS) {
                return Shapes.join(main, DOORS.get(dir), BooleanOp.OR);
            } else if (teI.inputProgress < PressureChamberInterfaceBlockEntity.MAX_PROGRESS) {
                return Shapes.join(main, DOORS.get(dir.getOpposite()), BooleanOp.OR);
            } else {
                return main;
            }
        }).orElse(main);
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
    public void setPlacedBy(Level par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.setPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (!par1World.isClientSide && PressureChamberValveBlockEntity.checkIfProperlyFormed(par1World, pos)) {
            ModCriterionTriggers.PRESSURE_CHAMBER.get().trigger((ServerPlayer) par5EntityLiving);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !world.isClientSide) {
            world.getBlockEntity(pos, ModBlockEntityTypes.PRESSURE_CHAMBER_INTERFACE.get())
                    .ifPresent(PressureChamberWallBlockEntity::onBlockBreak);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PressureChamberInterfaceBlockEntity(pPos, pState);
    }
}
