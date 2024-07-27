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
import me.desht.pneumaticcraft.common.block.entity.utility.ReinforcedChestBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReinforcedChestBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock, IBlockComparatorSupport {
    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 15, 15);

    public ReinforcedChestBlock() {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ReinforcedChestBlockEntity(pPos, pState);
    }

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        super.addSerializableComponents(list);
        list.add(ModDataComponents.BLOCK_ENTITY_SAVED_INV.get());
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!willHarvest && level.getBlockEntity(pos) instanceof ReinforcedChestBlockEntity be) {
            // broken with wrong tool; ensure any contents get dropped even if the block itself doesn't
            PneumaticCraftUtils.forceDropContents(level, pos, be.getItemHandler());
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    public static class ItemBlockReinforcedChest extends BlockItem implements IInventoryItem {
        public ItemBlockReinforcedChest(ReinforcedChestBlock block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks) {
            IInventoryItem.getStacks(stack.getOrDefault(ModDataComponents.BLOCK_ENTITY_SAVED_INV, ItemContainerContents.EMPTY), curStacks);
        }

        @Override
        public String getTooltipPrefix(ItemStack stack) {
            return ChatFormatting.GREEN.toString();
        }

        @Override
        public boolean canFitInsideContainerItems() {
            return false;
        }
    }
}
