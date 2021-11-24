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
                Block.box(0, 0, 0, 16, 3, 16),
                Block.box(0, 13, 0, 16, 16, 16),
                Block.box(0, 0, 0, 3, 16, 16),
                Block.box(13, 0, 0, 16, 16, 16),
                Block.box(3, 3, 0, 5, 5, 16),
                Block.box(11, 3, 0, 13, 5, 16),
                Block.box(3, 11, 0, 5, 13, 16),
                Block.box(11, 11, 0, 13, 13, 16)
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

    public BlockPressureChamberInterface() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction dir = getRotation(state);
        VoxelShape main = SHAPES.get(dir.getAxis());

        return PneumaticCraftUtils.getTileEntityAt(worldIn, pos, TileEntityPressureChamberInterface.class).map(teI -> {
            if (teI.outputProgress < TileEntityPressureChamberInterface.MAX_PROGRESS) {
                return VoxelShapes.join(main, DOORS.get(dir), IBooleanFunction.OR);
            } else if (teI.inputProgress < TileEntityPressureChamberInterface.MAX_PROGRESS) {
                return VoxelShapes.join(main, DOORS.get(dir.getOpposite()), IBooleanFunction.OR);
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
    public void setPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.setPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (!par1World.isClientSide && TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos)) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((ServerPlayerEntity) par5EntityLiving);
        }
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !world.isClientSide) {
            PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPressureChamberInterface.class)
                    .ifPresent(TileEntityPressureChamberWall::onBlockBreak);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}
