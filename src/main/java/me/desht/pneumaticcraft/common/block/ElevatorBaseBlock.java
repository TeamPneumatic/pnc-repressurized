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
import me.desht.pneumaticcraft.common.registry.ModBlocks;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ElevatorBaseBlock extends AbstractCamouflageBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape BASE = box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape TOP  = box(0, 15, 0, 16, 16, 16);
    private static final VoxelShape CORE = box(3, 1, 3, 13, 15, 13);
    private static final VoxelShape SHAPE = Shapes.or(BASE, CORE, TOP);

    public ElevatorBaseBlock() {
        super(ModBlocks.defaultProps().noOcclusion());  // notSolid() because of camo requirements
        registerDefaultState(defaultBlockState()
                .setValue(AbstractPneumaticCraftBlock.NORTH, false)
                .setValue(AbstractPneumaticCraftBlock.SOUTH, false)
                .setValue(AbstractPneumaticCraftBlock.WEST, false)
                .setValue(AbstractPneumaticCraftBlock.EAST, false)
        );
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public void onPlace(BlockState newState, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(newState, world, pos, oldState, isMoving);
        ElevatorBaseBlockEntity elevatorBase = getCoreTileEntity(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AbstractPneumaticCraftBlock.NORTH, AbstractPneumaticCraftBlock.SOUTH, AbstractPneumaticCraftBlock.WEST, AbstractPneumaticCraftBlock.EAST);
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        return super.use(state, world, getCoreElevatorPos(world, pos), player, hand, brtr);
    }

    private static BlockPos getCoreElevatorPos(Level world, BlockPos pos) {
        if (world.getBlockState(pos.relative(Direction.UP)).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
            return getCoreElevatorPos(world, pos.relative(Direction.UP));
        } else {
            return pos;
        }
    }

    public static ElevatorBaseBlockEntity getCoreTileEntity(Level world, BlockPos pos) {
        return (ElevatorBaseBlockEntity) world.getBlockEntity(getCoreElevatorPos(world, pos));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockState(pos.relative(Direction.DOWN)).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
                BlockEntity te = world.getBlockEntity(pos.relative(Direction.DOWN));
                ((ElevatorBaseBlockEntity) te).moveUpgradesFromAbove();
            }
            ElevatorBaseBlockEntity elevatorBase = getCoreTileEntity(world, pos);
            if (elevatorBase != null) {
                elevatorBase.updateMaxElevatorHeight();
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ElevatorBaseBlockEntity(pPos, pState);
    }
}
