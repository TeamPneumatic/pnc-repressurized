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

import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.common.block.entity.utility.SmartChestBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SmartChestBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock, IBlockComparatorSupport {
    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 15, 15);

    public SmartChestBlock() {
        super(ModBlocks.reinforcedStoneProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
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
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SmartChestBlockEntity(pPos, pState);
    }

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        super.addSerializableComponents(list);
        list.add(ModDataComponents.SMART_CHEST_SAVED.get());
    }

    public static class ItemBlockBlockSmartChest extends BlockItem implements IInventoryItem {
        public ItemBlockBlockSmartChest(Block block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks) {
            SmartChestBlockEntity.SavedData data = stack.getOrDefault(ModDataComponents.SMART_CHEST_SAVED, SmartChestBlockEntity.SavedData.EMPTY);
            IInventoryItem.getStacks(data.inventory(), curStacks);
        }

        @Override
        public String getTooltipPrefix(ItemStack stack) {
            return ChatFormatting.GREEN.toString();
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
            super.appendHoverText(stack, context, tooltip, flagIn);

            SmartChestBlockEntity.SavedData savedData = stack.get(ModDataComponents.SMART_CHEST_SAVED);
            if (savedData != null) {
                int filterSize = savedData.filter().getSlots();
                if (filterSize > 0) {
                    tooltip.add(xlate("pneumaticcraft.gui.tooltip.smartChest.filter", filterSize));
                }
                int lastSlot = savedData.lastSlot();
                if (lastSlot < SmartChestBlockEntity.CHEST_SIZE) {
                    tooltip.add(xlate("pneumaticcraft.gui.tooltip.smartChest.slotsClosed", SmartChestBlockEntity.CHEST_SIZE - lastSlot));
                }
            }
        }
    }
}
