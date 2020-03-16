package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerTagWorkbench;
import me.desht.pneumaticcraft.common.item.ItemTagFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class TileEntityTagWorkbench extends TileEntityDisplayTable implements INamedContainerProvider {
    public static final int PAPER_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    private final TagMatcherItemHandler inventory = new TagMatcherItemHandler();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);

    public int paperItemId;
    public int outputItemId;

    public TileEntityTagWorkbench() {
        super(ModTileEntities.TAG_WORKBENCH.get());
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
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
                Set<ResourceLocation> tags = ItemTagFilter.getConfiguredTagList(outputStack);
                for (String s : data) {
                    tags.add(new ResourceLocation(s));
                }
                ItemTagFilter.setConfiguredTagList(outputStack, tags);
                inventory.setStackInSlot(OUTPUT_SLOT, outputStack);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        itemId = Item.getIdFromItem(inventory.getStackInSlot(0).getItem());
        paperItemId = Item.getIdFromItem(inventory.getStackInSlot(1).getItem());
        outputItemId = Item.getIdFromItem(inventory.getStackInSlot(2).getItem());
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        paperItemId = tag.getInt("PaperItemId");
        outputItemId = tag.getInt("OutputItemId");
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        tag.putInt("PaperItemId", paperItemId);
        tag.putInt("OutputItemId", outputItemId);
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ContainerTagWorkbench(windowId, inv, getPos());
    }

    private class TagMatcherItemHandler extends DisplayItemHandler {
        TagMatcherItemHandler() {
            super(TileEntityTagWorkbench.this, 3);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            switch (slot) {
                case 0: return true;
                case PAPER_SLOT: return stack.getItem() == Items.PAPER || stack.getItem() == ModItems.TAG_FILTER.get();
                case OUTPUT_SLOT: return stack.getItem() == ModItems.TAG_FILTER.get();
                default: throw new IllegalArgumentException("invalid slot " + slot);
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (slot == 1) {
                outputItemId = Item.getIdFromItem(getStackInSlot(1).getItem());
            } else if (slot == 2) {
                paperItemId = Item.getIdFromItem(getStackInSlot(2).getItem());
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            switch (slot) {
                case 0:
                case OUTPUT_SLOT:
                    return 1;
                default:
                    return 64;

            }
        }
    }
}
