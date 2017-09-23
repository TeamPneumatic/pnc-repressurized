package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.NetworkUtils;
import me.desht.pneumaticcraft.common.network.PacketUpdateGui;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ContainerPneumaticBase<Tile extends TileEntityBase> extends Container implements IGUIButtonSensitive {

    public Tile te;
    private final List<SyncedField> syncedFields = new ArrayList<>();
    private boolean firstTick = true;
    private int playerSlotsStart;

    public ContainerPneumaticBase(Tile te) {
        this.te = te;
        if (te != null) addSyncedFields(te);
    }

    protected void addSyncedField(SyncedField field) {
        syncedFields.add(field);
        field.setLazy(false);
    }

    protected void addSyncedFields(Object annotatedObject) {
        List<SyncedField> fields = NetworkUtils.getSyncedFields(annotatedObject, GuiSynced.class);
        for (SyncedField field : fields)
            addSyncedField(field);
    }

    public void updateField(int index, Object value) {
        syncedFields.get(index).setValue(value);
        if (te != null) te.onGuiUpdate();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return te.isGuiUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < syncedFields.size(); i++) {
            if (syncedFields.get(i).update() || firstTick) {
                sendToContainerListeners(new PacketUpdateGui(i, syncedFields.get(i)));
            }
        }
        firstTick = false;
    }

    protected void sendToContainerListeners(IMessage message) {
        for (IContainerListener listener : listeners) {
            if (listener instanceof EntityPlayerMP) {
                NetworkHandler.sendTo(message, (EntityPlayerMP) listener);
            }
        }
    }

    protected void addPlayerSlots(InventoryPlayer inventoryPlayer, int yOffset) {
        playerSlotsStart = inventorySlots.size();

        // Add the player's inventory slots to the container
        for (int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for (int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, yOffset + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, yOffset + 58));
        }
    }

    protected void addUpgradeSlots(int xBase, int yBase) {
        for (int i = 0; i < te.getUpgradesInventory().getSlots(); i++) {
            addSlotToContainer(new SlotInventoryLimiting(te.getUpgradesInventory(), i, xBase + (i % 2) * 18, yBase + (i / 2) * 18));
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot srcSlot = inventorySlots.get(slot);

        if (srcSlot != null && srcSlot.getHasStack()) {
            ItemStack stackInSlot = srcSlot.getStack();
            var3 = stackInSlot.copy();

            if (slot < playerSlotsStart) {
                if (!mergeItemStack(stackInSlot, playerSlotsStart, playerSlotsStart + 36, false))
                    return ItemStack.EMPTY;

                srcSlot.onSlotChange(stackInSlot, var3);
            } else {
                /* int validStartSlot = -1; Unncessary, as Forge now adds respect to slot.isItemValid to mergeItemStack
                 for(int i = 0; i < playerSlotsStart; i++) {
                     boolean slotValid = inventorySlots.get(i).isItemValid(var5);
                     if(slotValid && validStartSlot == -1) {
                         validStartSlot = i;
                     } else if(!slotValid && validStartSlot >= 0) {
                         if(!mergeItemStack(var5, validStartSlot, i, false)) return null;
                         validStartSlot = -1;
                     }
                 }
                 if(validStartSlot >= 0) {
                     if(!mergeItemStack(var5, validStartSlot, playerSlotsStart, false)) return null;
                 }*/
                if (!mergeItemStack(stackInSlot, 0, playerSlotsStart, false))
                    return ItemStack.EMPTY;
                srcSlot.onSlotChange(stackInSlot, var3);
            }

            if (stackInSlot.isEmpty()) {
                srcSlot.putStack(ItemStack.EMPTY);
            } else {
                srcSlot.onSlotChanged();
            }

            if (stackInSlot.getCount() == var3.getCount())
                return ItemStack.EMPTY;

            srcSlot.onTake(player, stackInSlot);
        }

        return var3;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        Slot slot = slotId < 0 ? null : inventorySlots.get(slotId);
        if (slot instanceof IPhantomSlot) {
            return slotClickPhantom(slot, dragType, clickType, player);
        }
        return super.slotClick(slotId, dragType, clickType, player);

    }

    @Nonnull
    private ItemStack slotClickPhantom(Slot slot, int dragType, ClickType clickType, EntityPlayer player) {
        ItemStack stack = ItemStack.EMPTY;

        if (clickType == ClickType.CLONE && dragType == 2) {
            // middle-click: clear slot
            if (((IPhantomSlot) slot).canAdjust()) {
                slot.putStack(ItemStack.EMPTY);
            }
        } else if (clickType == ClickType.PICKUP && (dragType == 0 || dragType == 1)) {
            // left or right-click...
            InventoryPlayer playerInv = player.inventory;
            slot.onSlotChanged();
            ItemStack stackSlot = slot.getStack();
            ItemStack stackHeld = playerInv.getItemStack();

            stack = stackSlot.copy();
            if (stackSlot.isEmpty()) {
                if (!stackHeld.isEmpty() && slot.isItemValid(stackHeld)) {
                    fillPhantomSlot(slot, stackHeld, clickType, dragType);
                }
            } else if (stackHeld.isEmpty()) {
                adjustPhantomSlot(slot, clickType, dragType);
                slot.onTake(player, playerInv.getItemStack());
            } else if (slot.isItemValid(stackHeld)) {
                if (canStacksMerge(stackSlot, stackHeld)) {
                    adjustPhantomSlot(slot, clickType, dragType);
                } else {
                    fillPhantomSlot(slot, stackHeld, clickType, dragType);
                }
            }
        }
        return stack;
    }

    private boolean canStacksMerge(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (!stack1.isItemEqual(stack2)) return false;
        return ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    private void adjustPhantomSlot(Slot slot, ClickType clickType, int dragType) {
        if (!((IPhantomSlot) slot).canAdjust()) {
            return;
        }
        ItemStack stackSlot = slot.getStack().copy();
        if (dragType == 1) {
            // right-click: increase stack size
            stackSlot.setCount(Math.min(stackSlot.getCount() + 1, slot.getSlotStackLimit()));
        } else if (dragType == 0) {
            // left-click: decrease stack size
            stackSlot.shrink(1);
        }
        slot.putStack(stackSlot);
    }

    private void fillPhantomSlot(Slot slot, ItemStack stackHeld, ClickType clickType, int dragType) {
        if (!((IPhantomSlot) slot).canAdjust()) {
            return;
        }
        int stackSize = dragType == 0 ? stackHeld.getCount() : 1;
        if (stackSize > slot.getSlotStackLimit()) {
            stackSize = slot.getSlotStackLimit();
        }
        ItemStack phantomStack = stackHeld.copy();
        phantomStack.setCount(stackSize);

        slot.putStack(phantomStack);
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
        if (te != null) {
            te.handleGUIButtonPress(guiID, player);
        }
    }
}
