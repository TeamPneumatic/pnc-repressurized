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
import me.desht.pneumaticcraft.common.item.ItemDrillBit;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.item.ItemJackHammer.DigMode;
import me.desht.pneumaticcraft.common.tileentity.AbstractPneumaticCraftBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class JackhammerSetupMenu extends AbstractPneumaticCraftMenu<AbstractPneumaticCraftBlockEntity> {
    private final ItemJackHammer.DrillBitHandler drillBitHandler;
    private final ItemJackHammer.EnchantmentHandler enchantmentHandler;
    private final InteractionHand hand;

    public JackhammerSetupMenu(int windowId, Inventory invPlayer, FriendlyByteBuf buffer) {
        this(windowId, invPlayer, getHand(buffer));
    }

    public JackhammerSetupMenu(int windowId, Inventory invPlayer, InteractionHand hand) {
        super(ModMenuTypes.JACKHAMMER_SETUP.get(), windowId, invPlayer);
        this.hand = hand;

        drillBitHandler = ItemJackHammer.getDrillBitHandler(invPlayer.player.getItemInHand(hand));
        if (drillBitHandler != null) {
            addSlot(new SlotDrillBit(drillBitHandler, 0, 128, 19));
        }

        enchantmentHandler = ItemJackHammer.getEnchantmentHandler(invPlayer.player.getItemInHand(hand));
        if (enchantmentHandler != null) {
            addSlot(new SlotEnchantmentHandler(enchantmentHandler, 0, 96, 19));
        }

        addPlayerSlots(invPlayer, 100);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);

        drillBitHandler.save();
        enchantmentHandler.save();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() == ModItems.JACKHAMMER.get();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        ItemStack hammerStack = player.getItemInHand(hand);
        if (tag.startsWith("digmode:") && hammerStack.getItem() instanceof ItemJackHammer) {
            try {
                ItemDrillBit.DrillBitType ourBit = ((ItemJackHammer) hammerStack.getItem()).getDrillBit(hammerStack);
                DigMode newDigMode = DigMode.valueOf(tag.substring(8));
                if (ourBit.getHarvestLevel() >= newDigMode.getBitType().getHarvestLevel() || newDigMode == DigMode.MODE_1X1) {
                    ItemJackHammer.setDigMode(hammerStack, newDigMode);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public InteractionHand getHand() {
        return hand;
    }

    private static class SlotDrillBit extends SlotItemHandler {
        public SlotDrillBit(ItemJackHammer.DrillBitHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return stack.getItem() instanceof ItemDrillBit;
        }

        @Override
        public void setChanged() {
            ((ItemJackHammer.DrillBitHandler) getItemHandler()).save();
        }

        @Override
        public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {
            ((ItemJackHammer.DrillBitHandler) getItemHandler()).save();
        }
    }

    private static class SlotEnchantmentHandler extends SlotItemHandler {
        public SlotEnchantmentHandler(ItemJackHammer.EnchantmentHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return ItemJackHammer.EnchantmentHandler.validateBook(stack);
        }

        @Override
        public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {
            ((ItemJackHammer.EnchantmentHandler) getItemHandler()).save();
        }

        @Override
        public void setChanged() {
            ((ItemJackHammer.EnchantmentHandler) getItemHandler()).save();
        }
    }
}
