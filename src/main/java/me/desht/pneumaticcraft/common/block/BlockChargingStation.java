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

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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

    private static final VoxelShape BASE = Block.box(1, 0, 1, 15, 1, 15);
    private static final VoxelShape FRAME = Block.box(4, 1, 4, 12, 6, 12);
    private static final VoxelShape PAD_FRAME = Block.box(3, 1, 3, 13, 16, 13);
    private static final VoxelShape SHAPE = VoxelShapes.join(BASE, FRAME, IBooleanFunction.OR);
    private static final VoxelShape PAD_SHAPE = VoxelShapes.join(BASE, PAD_FRAME, IBooleanFunction.OR);

    public BlockChargingStation() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any().setValue(CHARGE_PAD, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHARGE_PAD);
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return state.getValue(CHARGE_PAD) ? PAD_SHAPE : SHAPE;
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
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return PneumaticCraftUtils.getTileEntityAt(blockAccess, pos, TileEntityChargingStation.class)
                .map(teCS -> teCS.getRedstoneController().shouldEmit() ? 15 : 0).orElse(0);
    }

    public static class ItemBlockChargingStation extends BlockItem {
        public ItemBlockChargingStation(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public String getDescriptionId(ItemStack stack) {
            CompoundNBT tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (tag != null && tag.getBoolean("UpgradeOnly")) {
                return super.getDescriptionId(stack) + ".upgrade_only";
            } else {
                return super.getDescriptionId(stack);
            }
        }

//        @Override
//        public ITextComponent getDisplayName(ItemStack stack) {
//            CompoundNBT tag = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
//            if (tag != null && tag.getBoolean("UpgradeOnly")) {
//                return super.getDisplayName(stack).deepCopy().appendString(" ").append(xlate("pneumaticcraft.gui.tooltip.charging_station.upgradesOnly"));
//            } else {
//                return super.getDisplayName(stack);
//            }
//        }
    }
}
