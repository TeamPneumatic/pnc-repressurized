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

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class BlockPneumaticDoorBase extends BlockPneumaticCraftCamo {
    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(0, 12, 14, 16, 16, 16),
            Block.box(14, 12, 0, 16, 16, 14),
            Block.box(0, 12, 0, 3, 16, 14)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public BlockPneumaticDoorBase() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoorBase.class;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(world, pos, state, entity, stack);
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPneumaticDoorBase.class).ifPresent(this::updateDoorSide);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPneumaticDoorBase.class).ifPresent(teDoorBase -> {
            updateDoorSide(teDoorBase);
            teDoorBase.onNeighborBlockUpdate(fromPos);
            BlockPos doorPos = pos.relative(teDoorBase.getRotation());
            BlockState doorState = world.getBlockState(doorPos);
            if (doorState.getBlock() instanceof BlockPneumaticDoor) {
                doorState.neighborChanged(world, doorPos, doorState.getBlock(), pos, false);
            }
        });
    }

    private void updateDoorSide(TileEntityPneumaticDoorBase doorBase) {
        PneumaticCraftUtils.getTileEntityAt(doorBase.getLevel(), doorBase.getBlockPos().relative(doorBase.getRotation()), TileEntityPneumaticDoor.class)
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
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return VoxelShapes.block();
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return PneumaticCraftUtils.getTileEntityAt(blockAccess, pos, TileEntityPneumaticDoorBase.class)
                .map(te -> te.shouldPassSignalToDoor() && side == te.getRotation().getOpposite() ? te.getCurrentRedstonePower() : 0)
                .orElse(0);
    }
}
