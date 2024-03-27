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

import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IGUIButtonSensitive;
import me.desht.pneumaticcraft.common.inventory.slot.UnstackablePhantomSlot;
import me.desht.pneumaticcraft.common.item.AmadronTabletItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class AmadronAddTradeMenu extends AbstractPneumaticCraftMenu<AbstractPneumaticCraftBlockEntity> implements IGUIButtonSensitive {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final ItemStackHandler inv = new ItemStackHandler(2);

    AmadronAddTradeMenu(int windowId, Inventory playerInventory) {
        super(ModMenuTypes.AMADRON_ADD_TRADE.get(), windowId, playerInventory);

        addSlot(new UnstackablePhantomSlot(inv, INPUT_SLOT, 37, 90));
        addSlot(new UnstackablePhantomSlot(inv, OUTPUT_SLOT, 126, 90));
    }

    public AmadronAddTradeMenu(int windowId, Inventory invPlayer, FriendlyByteBuf extraData) {
        this(windowId, invPlayer);
    }

    public void setStack(int index, @Nonnull ItemStack stack) {
        inv.setStackInSlot(index, stack);
    }

    @Nonnull
    public ItemStack getStack(int index) {
        return inv.getStackInSlot(index);
    }

    @Nonnull
    public ItemStack getInputStack() {
        return inv.getStackInSlot(INPUT_SLOT);
    }

    @Nonnull
    public ItemStack getOutputStack() {
        return inv.getStackInSlot(OUTPUT_SLOT);
    }

    @Override
    public boolean stillValid(Player player) {
        return getHand(player) != null;
    }

    @Override
    public void setItem(int slot, int state, @Nonnull ItemStack stack) {
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer playerIn) {
       if (tag.equals("showAmadron")) {
           AmadronTabletItem.openGui(playerIn, getHand(playerIn));
       }
    }

    private InteractionHand getHand(Player player) {
        if (player.getMainHandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
            return InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().getItem() == ModItems.AMADRON_TABLET.get()) {
            return InteractionHand.OFF_HAND;
        } else {
            return null;
        }
    }
}
