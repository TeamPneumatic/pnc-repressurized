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

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ContainerAmadronAddTrade extends ContainerPneumaticBase<TileEntityBase> implements IGUIButtonSensitive {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final ItemStackHandler inv = new ItemStackHandler(2);

    ContainerAmadronAddTrade(int windowId, Inventory playerInventory) {
        super(ModMenuTypes.AMADRON_ADD_TRADE.get(), windowId, playerInventory);

        addSlot(new SlotPhantomUnstackable(inv, INPUT_SLOT, 37, 90));
        addSlot(new SlotPhantomUnstackable(inv, OUTPUT_SLOT, 126, 90));
    }

    public ContainerAmadronAddTrade(int windowId, Inventory invPlayer, FriendlyByteBuf extraData) {
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
           ItemAmadronTablet.openGui(playerIn, getHand(playerIn));
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
