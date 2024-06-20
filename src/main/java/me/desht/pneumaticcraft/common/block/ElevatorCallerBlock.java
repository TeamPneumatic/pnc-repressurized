/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.elevator.ElevatorBaseBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.elevator.ElevatorCallerBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ElevatorCallerBlock extends AbstractCamouflageBlock implements PneumaticCraftEntityBlock {
    public ElevatorCallerBlock() {
        super(ModBlocks.defaultProps().noOcclusion());
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult brtr) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof ElevatorCallerBlockEntity teEC && !level.isClientSide) {
            int floor = getFloorForHit(teEC, brtr.getDirection(), brtr.getLocation().x, brtr.getLocation().y, brtr.getLocation().z);
            if (floor >= 0) setSurroundingElevators(level, pos, floor);
        }
        return getRotation(state).getOpposite() == brtr.getDirection() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private int getFloorForHit(ElevatorCallerBlockEntity teEC, Direction side, double hitX, double hitY, double hitZ) {
        double x;
        switch (side) {
            case NORTH: x = Math.abs(hitX % 1); break;
            case SOUTH: x = 1 - Math.abs(hitX % 1); break;
            case EAST: x = Math.abs(hitZ % 1); break;
            case WEST: x = 1 - Math.abs(hitZ % 1); break;
            default: return -1;
        }
        // yep, Y val seems to need inverting if Y < 0.  go figure?
        double y = hitY < 0 ? Math.abs(hitY % 1) : 1 - Math.abs(hitY % 1);

        for (ElevatorCallerBlockEntity.ElevatorButton button : teEC.getFloors()) {
            if (x >= button.posX && x <= button.posX + button.width && y >= button.posY && y <= button.posY + button.height) {
                return button.floorNumber;
            }
        }
        return -1;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    public static void setSurroundingElevators(Level world, BlockPos pos, int floor) {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            getElevatorBase(world, pos.relative(dir).relative(Direction.DOWN, 2)).ifPresent(te -> te.goToFloor(floor));
        }
    }

    @Override
    public void onPlace(BlockState newState, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(newState, world, pos, oldState, isMoving);

        updateElevatorButtons(world, pos);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        updateElevatorButtons(world, pos);

        super.onRemove(state, world, pos, newState, isMoving);
    }

    /**
     * Called when a caller is added or removed; detect any connected elevator base (by finding an adjacent frame
     * and following it down), and tell it to rescan for elevator callers.
     * @param world the world
     * @param pos the blockpos where the caller has been placed/removed
     */
    private void updateElevatorButtons(Level world, BlockPos pos) {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            boolean ok = getElevatorBase(world, pos.relative(dir).relative(Direction.DOWN, 2)).map(te -> {
                te.updateFloors(true);
                return true;
            }).orElse(false);
            if (ok) break;
        }
    }

    private static Optional<ElevatorBaseBlockEntity> getElevatorBase(Level world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == ModBlocks.ELEVATOR_FRAME.get()) {
            return ElevatorFrameBlock.getElevatorBase(world, pos);
        } else if (block == ModBlocks.ELEVATOR_BASE.get()) {
            return world.getBlockEntity(pos, ModBlockEntityTypes.ELEVATOR_BASE.get())
                    .filter(ElevatorBaseBlockEntity::isCoreElevator);
        }
        return Optional.empty();
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter pLevel, BlockPos pos, Direction side) {
        return pLevel.getBlockEntity(pos, ModBlockEntityTypes.ELEVATOR_CALLER.get())
                .map(teEc -> teEc.getEmittingRedstone() ? 15 : 0)
                .orElse(0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ElevatorCallerBlockEntity(pPos, pState);
    }
}
