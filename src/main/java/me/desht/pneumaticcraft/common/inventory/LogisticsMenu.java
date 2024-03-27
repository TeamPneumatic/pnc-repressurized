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

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.inventory.slot.PhantomSlot;
import me.desht.pneumaticcraft.common.inventory.slot.UnstackablePhantomSlot;
import me.desht.pneumaticcraft.common.item.logistics.AbstractLogisticsFrameItem;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.semiblock.ISyncableSemiblockItem;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class LogisticsMenu extends AbstractPneumaticCraftMenu<AbstractPneumaticCraftBlockEntity> implements ISyncableSemiblockItem {
    public final AbstractLogisticsFrameEntity logistics;
    private final boolean itemContainer;  // true if GUI opened from held item, false if from in-world entity

    public LogisticsMenu(MenuType<?> containerType, int i, Inventory playerInventory, int entityId) {
        super(containerType, i, playerInventory);

        Level world = playerInventory.player.level();
        if (entityId == -1) {
            // opening container from held item; no in-world entity so fake one up from the held item NBT
            this.logistics = AbstractLogisticsFrameEntity.fromItemStack(world, playerInventory.player, getHeldLogisticsFrame(playerInventory.player));
            this.itemContainer = true;
        } else {
            Entity e = world.getEntity(entityId);
            if (e instanceof AbstractLogisticsFrameEntity) {
                this.logistics = (AbstractLogisticsFrameEntity) e;
            } else {
                this.logistics = null;
                Log.error("no logistics frame entity for id %d!", entityId);
            }
            this.itemContainer = false;
        }
        if (logistics != null) {
            IItemHandler requests = logistics.getItemFilterHandler();
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 9; x++) {
                    addSlot(logistics.canFilterStack() ?
                            new PhantomSlot(requests, y * 9 + x, x * 18 + 8, y * 18 + 29) :
                            new UnstackablePhantomSlot(requests, y * 9 + x, x * 18 + 8, y * 18 + 29));
                }
            }

            addPlayerSlots(playerInventory, 134);
        }
    }

    private LogisticsMenu(MenuType logisticsFrameRequester, int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(logisticsFrameRequester, i, playerInventory, buffer.readVarInt());
    }

    private ItemStack getHeldLogisticsFrame(Player player) {
        if (player.getMainHandItem().getItem() instanceof AbstractLogisticsFrameItem) {
            return player.getMainHandItem();
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof AbstractLogisticsFrameItem) {
            return player.getItemInHand(InteractionHand.OFF_HAND);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public boolean isItemContainer() {
        return itemContainer;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        super.handleGUIButtonPress(tag, shiftHeld, player);
        if (logistics != null) {
            logistics.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return logistics != null && logistics.isValid();
    }

    /**
     * Called when the container is closed. If configuring a logistics frame in-hand, update its NBT now.
     */
    @Override
    public void removed(Player player) {
        if (itemContainer && logistics != null && !player.getCommandSenderWorld().isClientSide) {
            syncSemiblockItemFromClient(player, null);
        }
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot srcSlot = slots.get(slotIndex);
        if (slotIndex >= playerSlotsStart && srcSlot != null && srcSlot.hasItem()) {
            // shift-click from player inventory into filter
            ItemStack stackInSlot = srcSlot.getItem();
            for (int i = 0; i < 27; i++) {
                Slot slot = slots.get(i);
                if (!slot.hasItem()) {
                    ItemStack s = logistics.canFilterStack() ?
                            stackInSlot.copy() :
                            ItemHandlerHelper.copyStackWithSize(stackInSlot, 1);
                    slot.set(s);
                    break;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static LogisticsMenu createProviderContainer(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new LogisticsMenu(ModMenuTypes.LOGISTICS_FRAME_PROVIDER.get(), i, playerInventory, buffer);
    }

    public static LogisticsMenu createRequesterContainer(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new LogisticsMenu(ModMenuTypes.LOGISTICS_FRAME_REQUESTER.get(), i, playerInventory, buffer);
    }

    public static LogisticsMenu createStorageContainer(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new LogisticsMenu(ModMenuTypes.LOGISTICS_FRAME_STORAGE.get(), i, playerInventory, buffer);
    }

    @Override
    public void syncSemiblockItemFromClient(Player player, FriendlyByteBuf payload) {
        if (logistics != null) {
            if (payload != null) logistics.readFromBuf(payload);
            ItemStack stack = getHeldLogisticsFrame(player);
            if (!stack.isEmpty()) {
                CompoundTag subtag = logistics.serializeNBT(new CompoundTag());
                stack.getOrCreateTag().put(NBTKeys.ENTITY_TAG, subtag);
            }
        }
    }
}
