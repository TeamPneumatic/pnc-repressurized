package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ContainerPneumaticBase<T extends TileEntityBase> extends Container implements IGUIButtonSensitive {
    public final T te;
    private final List<SyncedField<?>> syncedFields = new ArrayList<>();
    private boolean firstTick = true;
    int playerSlotsStart;

    public ContainerPneumaticBase(ContainerType type, int windowId, PlayerInventory invPlayer, PacketBuffer extraData) {
        this(type, windowId, invPlayer, getTilePos(extraData));
    }

    public ContainerPneumaticBase(ContainerType type, int windowId, PlayerInventory invPlayer) {
        this(type, windowId, invPlayer, (BlockPos) null);
    }

    public ContainerPneumaticBase(ContainerType type, int windowId, PlayerInventory invPlayer, BlockPos tilePos) {
        super(type, windowId);
        if (tilePos != null) {
            TileEntity te0 = invPlayer.player.level.getBlockEntity(tilePos);
            if (te0 instanceof TileEntityBase) {
                //noinspection unchecked
                te = (T) te0;  // should be safe: T extends TileEntityBase, and we're doing an instanceof
                addSyncedFields(te);
            } else {
                te = null;
            }
        } else {
            te = null;
        }
    }

    public static void putHand(PacketBuffer buf, Hand hand) {
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    static Hand getHand(PacketBuffer buf) { return buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND; }

    static BlockPos getTilePos(PacketBuffer buf) {
        return buf.readBlockPos();
    }

    void addSyncedField(SyncedField<?> field) {
        syncedFields.add(field);
        field.setLazy(false);
    }

    void addSyncedFields(Object annotatedObject) {
        NetworkUtils.getSyncedFields(annotatedObject, GuiSynced.class).forEach(this::addSyncedField);
    }

    public void updateField(int index, Object value) {
        syncedFields.get(index).setValue(value);
        if (te != null) te.onGuiUpdate();
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return te.isGuiUseableByPlayer(player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        for (int i = 0; i < syncedFields.size(); i++) {
            if (syncedFields.get(i).update() || firstTick) {
                sendToContainerListeners(new PacketUpdateGui(i, syncedFields.get(i)));
            }
        }
        firstTick = false;
    }

    void sendToContainerListeners(Object message) {
        for (IContainerListener listener : containerListeners) {
            if (listener instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(message, (ServerPlayerEntity) listener);
            }
        }
    }

    protected void addPlayerSlots(PlayerInventory inventoryPlayer, int yOffset) {
        addPlayerSlots(inventoryPlayer, 8, yOffset);
    }

    protected void addPlayerSlots(PlayerInventory inventoryPlayer, int xOffset, int yOffset) {
        playerSlotsStart = slots.size();

        // Add the player's inventory slots to the container
        for (int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for (int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlot(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, xOffset + inventoryColumnIndex * 18, yOffset + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlot(new Slot(inventoryPlayer, actionBarSlotIndex, xOffset + actionBarSlotIndex * 18, yOffset + 58));
        }
    }

    protected void addUpgradeSlots(int xBase, int yBase) {
        for (int i = 0; i < te.getUpgradeHandler().getSlots(); i++) {
            addSlot(new SlotUpgrade(te, i, xBase + (i % 2) * 18, yBase + (i / 2) * 18));
        }
    }

    /*
     * This is pretty much lifted from the ContainerPlayer constructor
     * We can't use EntityArmorInvWrapper because the wrapped setStackInSlot() method always sends a "set slot"
     * packet to the client when the item changes in any way.  Altering pressure in a pneumatic armor piece will cause
     * that packet to be sent continually, causing a horrible item-equip sound loop to be played (and using unnecessary
     * network bandwith).
     */
    @SuppressWarnings("SameParameterValue")
    void addArmorSlots(PlayerInventory inventoryPlayer, int xBase, int yBase) {
        for (int i = 0; i < 4; ++i) {
            this.addSlot(new SlotPlayer(inventoryPlayer, ArmorUpgradeRegistry.ARMOR_SLOTS[i], xBase, yBase + i * 18));
        }
    }

    void addOffhandSlot(PlayerInventory inventory, int x, int y) {
        this.addSlot(new SlotPlayer(inventory, EquipmentSlotType.OFFHAND, x, y));
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(PlayerEntity player, int slot) {
        Slot srcSlot = slots.get(slot);
        if (srcSlot == null || !srcSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack srcStack = srcSlot.getItem().copy();
        ItemStack copyOfSrcStack = srcStack.copy();

        if (slot < playerSlotsStart) {
            if (!moveItemStackTo(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(srcStack, 0, playerSlotsStart, false))
                return ItemStack.EMPTY;
        }

        srcSlot.set(srcStack);
        srcSlot.onQuickCraft(srcStack, copyOfSrcStack);
        srcSlot.onTake(player, srcStack);

        return copyOfSrcStack;
    }

    // almost the same as the super method, but pays attention to slot itemstack limits
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && consideredTheSameItem(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    // modified HERE
                    int maxSize = Math.min(slot.getMaxStackSize(itemstack), Math.min(slot.getMaxStackSize(), stack.getMaxStackSize()));
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    // modified HERE
                    int limit = Math.min(slot1.getMaxStackSize(), slot1.getMaxStackSize(stack));
                    if (stack.getCount() > limit) {
                        slot1.set(stack.split(limit));
                    } else {
                        slot1.set(stack.split(stack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Nonnull
    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
        Slot slot = slotId < 0 ? null : slots.get(slotId);
        if (slot instanceof IPhantomSlot) {
            return slotClickPhantom(slot, dragType, clickType, player);
        }
        return super.clicked(slotId, dragType, clickType, player);
    }

    @Nonnull
    private ItemStack slotClickPhantom(Slot slot, int dragType, ClickType clickType, PlayerEntity player) {
        ItemStack stack = ItemStack.EMPTY;

        if (clickType == ClickType.CLONE && dragType == 2) {
            // middle-click: clear slot
            if (((IPhantomSlot) slot).canAdjust()) {
                slot.set(ItemStack.EMPTY);
            }
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
            // left or right-click...
            PlayerInventory playerInv = player.inventory;
            slot.setChanged();
            ItemStack stackSlot = slot.getItem();
            ItemStack stackHeld = playerInv.getCarried();

            stack = stackSlot.copy();
            if (stackSlot.isEmpty()) {
                if (!stackHeld.isEmpty() && slot.mayPlace(stackHeld)) {
                    fillPhantomSlot(slot, stackHeld, dragType);
                }
            } else if (stackHeld.isEmpty()) {
                adjustPhantomSlot(slot, clickType, dragType);
                slot.onTake(player, playerInv.getCarried());
            } else if (slot.mayPlace(stackHeld)) {
                if (canStacksMerge(stackSlot, stackHeld)) {
                    adjustPhantomSlot(slot, clickType, dragType);
                } else {
                    fillPhantomSlot(slot, stackHeld, dragType);
                }
            }
        }
        return stack;
    }

    private boolean canStacksMerge(ItemStack stack1, ItemStack stack2) {
        return !(stack1.isEmpty() || stack2.isEmpty()) && stack1.sameItem(stack2) && ItemStack.tagMatches(stack1, stack2);
    }

    private void adjustPhantomSlot(Slot slot, ClickType clickType, int dragType) {
        if (!((IPhantomSlot) slot).canAdjust()) {
            return;
        }
        ItemStack stackSlot = slot.getItem().copy();
        if (dragType == 1) {
            if (clickType == ClickType.QUICK_MOVE) {
                stackSlot.setCount(Math.min(stackSlot.getCount() * 2, slot.getMaxStackSize())); // shift-r-click: double stack size
            } else {
                stackSlot.setCount(Math.min(stackSlot.getCount() + 1, slot.getMaxStackSize())); // r-click: increase stack size
            }
        } else if (dragType == 0) {
            if (clickType == ClickType.QUICK_MOVE) {
                stackSlot.setCount(stackSlot.getCount() / 2); // shift-l-click: half stack size
            } else {
                stackSlot.shrink(1); // l-click: decrease stack size
            }
        }
        slot.set(stackSlot);
    }

    private void fillPhantomSlot(Slot slot, ItemStack stackHeld, int dragType) {
        if (!((IPhantomSlot) slot).canAdjust()) {
            return;
        }
        int stackSize = dragType == 0 ? stackHeld.getCount() : 1;
        if (stackSize > slot.getMaxStackSize()) {
            stackSize = slot.getMaxStackSize();
        }
        ItemStack phantomStack = stackHeld.copy();
        phantomStack.setCount(stackSize);

        slot.set(phantomStack);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (te != null) {
            te.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }
}
