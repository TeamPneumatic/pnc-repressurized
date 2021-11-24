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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Predicate;
import java.util.function.Supplier;


public class ItemChestUpgradeKit extends Item {
    private final Supplier<? extends Block> chestBlock;
    private final Predicate<Block> blockPredicate;

    private ItemChestUpgradeKit(Properties properties, Supplier<? extends Block> chestBlock, Predicate<Block> blockPredicate) {
        super(properties);

        this.chestBlock = chestBlock;
        this.blockPredicate = blockPredicate;
    }

    protected void onUpgraded(BlockState oldState, ItemUseContext context) {
        if (oldState.getBlock().is(Tags.Blocks.CHESTS_WOODEN)) {
            // give back one wooden chest, since the upgrade kit cost a chest to make
            PneumaticCraftUtils.dropItemOnGround(new ItemStack(oldState.getBlock()), context.getLevel(), context.getClickedPos().relative(context.getClickedFace()));
        }
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        final World world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = world.getBlockState(pos);
        if (blockPredicate.test(state.getBlock())) {
            if (!world.isClientSide) {
                Direction facing = state.hasProperty(HorizontalBlock.FACING) ? state.getValue(HorizontalBlock.FACING) : Direction.NORTH;

                // 1. copy & clear the existing inventory
                TileEntity te = world.getBlockEntity(pos);
                NonNullList<ItemStack> inv = NonNullList.create();
                if (te != null) {
                    te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                        Pair<Integer,Integer> range = getInvRange(state, handler);
                        for (int i = range.getLeft(); i < range.getRight(); i++) {
                            inv.add(handler.extractItem(i, Integer.MAX_VALUE, false));
                        }
                    });
                }

                // 2. replace the (now empty) wooden chest with the upgraded chest
                BlockState newState = chestBlock.get().defaultBlockState();
                world.setBlockAndUpdate(pos, newState.setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
                if (context.getPlayer() instanceof ServerPlayerEntity) {
                    world.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                    world.levelEvent(context.getPlayer(), Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getId(newState));
                }

                // 3. fill the upgraded chest with the copied inventory
                PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityBase.class).ifPresent(teRC -> {
                    IItemHandler chestInv = teRC.getPrimaryInventory();
                    for (int i = 0; i < inv.size(); i++) {
                        if (i < chestInv.getSlots()) {
                            chestInv.insertItem(i, inv.get(i), false);
                        } else {
                            // just in case...
                            PneumaticCraftUtils.dropItemOnGround(inv.get(i), world, pos.relative(context.getClickedFace()));
                        }
                    }
                });

                onUpgraded(state, context);

                context.getItemInHand().shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    private Pair<Integer,Integer> getInvRange(BlockState state, IItemHandler handler) {
        if (state.getBlock() == Blocks.CHEST && state.hasProperty(ChestBlock.TYPE)) {
            // special case for (possibly double) vanilla chests
            switch (state.getValue(ChestBlock.TYPE)) {
                case RIGHT: return Pair.of(0, handler.getSlots() / 2);
                case LEFT: return Pair.of(handler.getSlots() / 2, handler.getSlots());
                case SINGLE: return Pair.of(0, handler.getSlots());
            }
        }
        return Pair.of(0, handler.getSlots());
    }

    public static class ItemReinforcedChestKit extends ItemChestUpgradeKit {
        public ItemReinforcedChestKit() {
            super(ModItems.defaultProps(), ModBlocks.REINFORCED_CHEST, b -> b.is(Tags.Blocks.CHESTS_WOODEN));
        }
    }

    public static class ItemSmartChestKit extends ItemChestUpgradeKit {
        public ItemSmartChestKit() {
            super(ModItems.defaultProps(), ModBlocks.SMART_CHEST, b -> b.is(Tags.Blocks.CHESTS_WOODEN) || b == ModBlocks.REINFORCED_CHEST.get());
        }

        @Override
        protected void onUpgraded(BlockState oldState, ItemUseContext context) {
            if (oldState.getBlock() == ModBlocks.REINFORCED_CHEST.get()) {
                // give back one reinforced chest, since the smart chest upgrade kit cost two reinforced chests to make
                PneumaticCraftUtils.dropItemOnGround(new ItemStack(ModBlocks.REINFORCED_CHEST.get()), context.getLevel(), context.getClickedPos().relative(context.getClickedFace()));
            } else {
                super.onUpgraded(oldState, context);
            }
        }
    }
}
