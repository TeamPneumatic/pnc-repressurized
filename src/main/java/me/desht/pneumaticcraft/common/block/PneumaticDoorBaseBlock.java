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

import me.desht.pneumaticcraft.common.block.entity.PneumaticDoorBaseBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PneumaticDoorBaseBlock extends AbstractCamouflageBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(0, 12, 14, 16, 16, 16),
            Block.box(14, 12, 0, 16, 16, 14),
            Block.box(0, 12, 0, 3, 16, 14)
    );
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public PneumaticDoorBaseBlock() {
        super(ModBlocks.defaultProps().noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);

        world.getBlockEntity(pos, ModBlockEntities.PNEUMATIC_DOOR_BASE.get()).ifPresent(this::updateDoorSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        world.getBlockEntity(pos, ModBlockEntities.PNEUMATIC_DOOR_BASE.get()).ifPresent(teDoorBase -> {
            updateDoorSide(teDoorBase);
            teDoorBase.onNeighborBlockUpdate(fromPos);
            BlockPos doorPos = pos.relative(teDoorBase.getRotation());
            BlockState doorState = world.getBlockState(doorPos);
            if (doorState.getBlock() instanceof PneumaticDoorBlock) {
                doorState.neighborChanged(world, doorPos, doorState.getBlock(), pos, false);
            }
        });
    }

    private void updateDoorSide(PneumaticDoorBaseBlockEntity doorBase) {
        doorBase.nonNullLevel().getBlockEntity(doorBase.getBlockPos().relative(doorBase.getRotation()), ModBlockEntities.PNEUMATIC_DOOR.get())
                .ifPresent(teDoor -> {
                    if (doorBase.getRotation().getClockWise() == teDoor.getRotation() && teDoor.rightGoing
                            || doorBase.getRotation().getCounterClockWise() == teDoor.getRotation() && !teDoor.rightGoing) {
                        teDoor.rightGoing = !teDoor.rightGoing;
                        teDoor.setRotationAngle(0);
                        teDoor.setChanged();
                    }
                });
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockAccess.getBlockEntity(pos, ModBlockEntities.PNEUMATIC_DOOR_BASE.get())
                .map(te -> te.shouldPassSignalToDoor() && side == te.getRotation().getOpposite() ? te.getCurrentRedstonePower() : 0)
                .orElse(0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PneumaticDoorBaseBlockEntity(pPos, pState);
    }
}
