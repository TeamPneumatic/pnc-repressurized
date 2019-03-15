package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ContainerLogistics extends ContainerPneumaticBase {
    public final SemiBlockLogistics logistics;
    private final boolean itemContainer;

    public ContainerLogistics(InventoryPlayer inventoryPlayer, SemiBlockLogistics logistics) {
        super(null);
        itemContainer = logistics == null;
        if (itemContainer) {
            logistics = getLogistics(inventoryPlayer.player, getHeldLogisticsFrame(inventoryPlayer.player));
        }
        this.logistics = logistics;
        if (logistics != null) {
            addSyncedFields(logistics);
            IItemHandler requests = logistics.getFilters();
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 9; x++) {
                    addSlotToContainer(logistics.canFilterStack() ?
                            new SlotPhantom(requests, y * 9 + x, x * 18 + 8, y * 18 + 29) :
                            new SlotPhantomUnstackable(requests, y * 9 + x, x * 18 + 8, y * 18 + 29));
                }
            }

            addPlayerSlots(inventoryPlayer, 134);
        }
    }

    private static SemiBlockLogistics getLogistics(EntityPlayer player, ItemStack stack) {
        return getLogistics(player.world, player, stack);
    }

    public static SemiBlockLogistics getLogistics(World world, ItemStack stack) {
        return getLogistics(world, null, stack);
    }

    private static SemiBlockLogistics getLogistics(World world, EntityPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof ItemLogisticsFrame) {
            SemiBlockLogistics logistics = (SemiBlockLogistics) SemiBlockManager.getSemiBlockForKey(((ItemLogisticsFrame) stack.getItem()).semiBlockId);
            if (logistics != null) {
                logistics.initialize(world, new BlockPos(0, 0, 0));
                logistics.onPlaced(player, stack, null);
                return logistics;
            }
        }
        return null;
    }

    public boolean isItemContainer() {
        return itemContainer;
    }

    /**
     * Called when the container is closed. If configuring a logistics frame in-hand, update its NBT now.
     */
    @Override
    public void onContainerClosed(EntityPlayer player) {
        if (itemContainer && logistics != null) {
            ItemStack logisticsStack = getHeldLogisticsFrame(player);
            if (!logisticsStack.isEmpty()) {
                NonNullList<ItemStack> drops = NonNullList.create();
                logistics.addDrops(drops);
                NBTTagCompound settingTag = drops.get(0).getTagCompound();
                logisticsStack.setTagCompound(settingTag != null ? settingTag.copy() : null);
            }
        }
    }

    private ItemStack getHeldLogisticsFrame(EntityPlayer player) {
        if (player.getHeldItemMainhand().getItem() instanceof ItemLogisticsFrame) {
            return player.getHeldItemMainhand();
        } else if (player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemLogisticsFrame) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
        super.handleGUIButtonPress(guiID, player);
        if (logistics != null) {
            logistics.handleGUIButtonPress(guiID, player);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return logistics != null && !logistics.isInvalid();
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotIndex) {
        Slot srcSlot = inventorySlots.get(slotIndex);
        if (slotIndex >= playerSlotsStart && srcSlot != null && srcSlot.getHasStack()) {
            // shift-click from player inventory into filter
            ItemStack stackInSlot = srcSlot.getStack();
            for (int i = 0; i < 27; i++) {
                Slot slot = inventorySlots.get(i);
                if (!slot.getHasStack()) {
                    slot.putStack(stackInSlot.copy());
                    slot.getStack().setCount(slot.getSlotStackLimit());
                    break;
                }
            }
        }

        return ItemStack.EMPTY;
    }

}
