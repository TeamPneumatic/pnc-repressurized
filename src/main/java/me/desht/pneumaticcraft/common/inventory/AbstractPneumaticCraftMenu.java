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

package me.desht.pneumaticcraft.common.inventory;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IGUIButtonSensitive;
import me.desht.pneumaticcraft.common.inventory.slot.IPhantomSlot;
import me.desht.pneumaticcraft.common.inventory.slot.PlayerEquipmentSlot;
import me.desht.pneumaticcraft.common.inventory.slot.UpgradeSlot;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPneumaticCraftMenu<T extends AbstractPneumaticCraftBlockEntity> extends AbstractContainerMenu implements IGUIButtonSensitive {
    public final T te;
    private final List<SyncedField<?>> syncedFields = new ArrayList<>();
    private boolean firstTick = true;
    int playerSlotsStart;

    public AbstractPneumaticCraftMenu(MenuType type, int windowId, Inventory invPlayer, FriendlyByteBuf extraData) {
        this(type, windowId, invPlayer, getTilePos(extraData));
    }

    public AbstractPneumaticCraftMenu(MenuType type, int windowId, Inventory invPlayer) {
        this(type, windowId, invPlayer, (BlockPos) null);
    }

    public AbstractPneumaticCraftMenu(MenuType type, int windowId, Inventory invPlayer, BlockPos tilePos) {
        super(type, windowId);
        if (tilePos != null) {
            BlockEntity te0 = invPlayer.player.level().getBlockEntity(tilePos);
            if (te0 instanceof AbstractPneumaticCraftBlockEntity) {
                //noinspection unchecked
                te = (T) te0;  // should be safe: T extends AbstractPneumaticCraftBlockEntity, and we're doing an instanceof
                addSyncedFields(te);
            } else {
                te = null;
            }
        } else {
            te = null;
        }
    }

    public static void putHand(FriendlyByteBuf buf, InteractionHand hand) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    static InteractionHand getHand(FriendlyByteBuf buf) { return buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND; }

    static BlockPos getTilePos(FriendlyByteBuf buf) {
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
    public boolean stillValid(Player player) {
        return te.isGuiUseableByPlayer(player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        IntList toUpdate = new IntArrayList();
        for (int i = 0; i < syncedFields.size(); i++) {
            if (syncedFields.get(i).update() || firstTick) {
                toUpdate.add(i);
            }
        }
        if (!toUpdate.isEmpty()) {
            final AbstractPneumaticCraftMenu<?> self = this;
            List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream()
                    .filter(p -> p.containerMenu == self)
                    .toList();
            if (!players.isEmpty()) {
                toUpdate.forEach(idx ->
                        players.forEach(player ->
                                NetworkHandler.sendToPlayer(new PacketUpdateGui(idx, syncedFields.get(idx)), player))
                );
            }
        }
        firstTick = false;
    }

    protected void addPlayerSlots(Inventory inventoryPlayer, int yOffset) {
        addPlayerSlots(inventoryPlayer, 8, yOffset);
    }

    protected void addPlayerSlots(Inventory inventoryPlayer, int xOffset, int yOffset) {
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
            addSlot(new UpgradeSlot(te, i, xBase + (i % 2) * 18, yBase + (i / 2) * 18));
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
    void addArmorSlots(Inventory inventoryPlayer, int xBase, int yBase) {
        for (int i = 0; i < 4; ++i) {
            this.addSlot(new PlayerEquipmentSlot(inventoryPlayer, ArmorUpgradeRegistry.ARMOR_SLOTS[i], xBase, yBase + i * 18));
        }
    }

    @SuppressWarnings("SameParameterValue")
    void addOffhandSlot(Inventory inventory, int x, int y) {
        this.addSlot(new PlayerEquipmentSlot(inventory, EquipmentSlot.OFFHAND, x, y));
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(Player player, int slot) {
        Slot srcSlot = slots.get(slot);
        if (srcSlot == null || !srcSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack srcStack = srcSlot.getItem().copy();
        ItemStack copyOfSrcStack = srcStack.copy();

        if (slot < playerSlotsStart) {
            if (!moveItemStackToHotbarOrInventory(srcStack, playerSlotsStart))
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

    boolean moveItemStackToHotbarOrInventory(ItemStack stack, int startIndex) {
        return moveItemStackTo(stack, startIndex + 27, startIndex + 36, false)
                || moveItemStackTo(stack, startIndex, startIndex + 27, false);
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
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
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

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        Slot slot = slotId < 0 ? null : slots.get(slotId);
        if (slot instanceof IPhantomSlot) {
            slotClickPhantom(slot, dragType, clickType, player);
        } else {
            super.clicked(slotId, dragType, clickType, player);
        }
    }

    @Nonnull
    private ItemStack slotClickPhantom(Slot slot, int dragType, ClickType clickType, Player player) {
        ItemStack stack = ItemStack.EMPTY;

        if (clickType == ClickType.CLONE && dragType == 2) {
            // middle-click: clear slot
            if (((IPhantomSlot) slot).canAdjust()) {
                slot.set(ItemStack.EMPTY);
            }
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
            // left or right-click...
            slot.setChanged();
            ItemStack stackSlot = slot.getItem();
            ItemStack stackHeld = getCarried();

            stack = stackSlot.copy();
            if (stackSlot.isEmpty()) {
                if (!stackHeld.isEmpty() && slot.mayPlace(stackHeld)) {
                    fillPhantomSlot(slot, stackHeld, dragType);
                }
            } else if (stackHeld.isEmpty()) {
                adjustPhantomSlot(slot, clickType, dragType);
                slot.onTake(player, getCarried());
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
        return !(stack1.isEmpty() || stack2.isEmpty()) && ItemStack.isSameItemSameTags(stack1, stack2);
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (te != null) {
            te.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }
}
