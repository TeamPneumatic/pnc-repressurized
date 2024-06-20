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

package me.desht.pneumaticcraft.common.block.entity.utility;

import me.desht.pneumaticcraft.common.inventory.TagWorkbenchMenu;
import me.desht.pneumaticcraft.common.item.TagFilterItem;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TagWorkbenchBlockEntity extends DisplayTableBlockEntity implements MenuProvider {
    public static final int PAPER_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    private final TagMatcherItemHandler inventory = new TagMatcherItemHandler();

    public int paperItemId;
    public int outputItemId;

    public TagWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.TAG_WORKBENCH.get(), pos, state);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inventory;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (tag.startsWith("write:")) {
            String[] data = tag.substring(6).split(",");
            if (data.length == 0) return;
            ItemStack outputStack = ItemStack.EMPTY;
            if (!inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
                outputStack = inventory.getStackInSlot(OUTPUT_SLOT);
            } else if (!inventory.getStackInSlot(PAPER_SLOT).isEmpty()) {
                inventory.extractItem(PAPER_SLOT, 1, false);
                outputStack = new ItemStack(ModItems.TAG_FILTER.get());
            }
            if (!outputStack.isEmpty()) {
                Set<TagKey<Item>> tags = new HashSet<>(TagFilterItem.getConfiguredTagList(outputStack));
                for (String s : data) {
                    tags.add(TagKey.create(Registries.ITEM, new ResourceLocation(s)));
                }
                TagFilterItem.setConfiguredTagList(outputStack, tags);
                inventory.setStackInSlot(OUTPUT_SLOT, outputStack);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Items", inventory.serializeNBT(provider));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        inventory.deserializeNBT(provider, tag.getCompound("Items"));
        displayedStack = inventory.getStackInSlot(0);
        paperItemId = Item.getId(inventory.getStackInSlot(1).getItem());
        outputItemId = Item.getId(inventory.getStackInSlot(2).getItem());
    }

    @Override
    public void readFromPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.readFromPacket(tag, provider);

        paperItemId = tag.getInt("PaperItemId");
        outputItemId = tag.getInt("OutputItemId");
    }

    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeToPacket(tag, provider);

        tag.putInt("PaperItemId", paperItemId);
        tag.putInt("OutputItemId", outputItemId);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new TagWorkbenchMenu(windowId, inv, getBlockPos());
    }

    private class TagMatcherItemHandler extends DisplayItemHandler {
        TagMatcherItemHandler() {
            super(TagWorkbenchBlockEntity.this, 3);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return switch (slot) {
                case 0 -> true;
                case PAPER_SLOT -> stack.getItem() == Items.PAPER || stack.getItem() == ModItems.TAG_FILTER.get();
                case OUTPUT_SLOT -> stack.getItem() == ModItems.TAG_FILTER.get();
                default -> throw new IllegalArgumentException("invalid slot " + slot);
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (slot == 1) {
                outputItemId = Item.getId(getStackInSlot(1).getItem());
            } else if (slot == 2) {
                paperItemId = Item.getId(getStackInSlot(2).getItem());
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (slot) {
                case 0, OUTPUT_SLOT -> 1;
                default -> 64;
            };
        }
    }
}
