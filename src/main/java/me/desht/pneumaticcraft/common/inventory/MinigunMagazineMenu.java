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
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class MinigunMagazineMenu extends AbstractPneumaticCraftMenu<AbstractPneumaticCraftBlockEntity> {
    private final MinigunItem.MagazineHandler gunInv;
    private final InteractionHand hand;

    public MinigunMagazineMenu(int i, Inventory playerInventory, @SuppressWarnings("unused") FriendlyByteBuf buffer) {
        this(i, playerInventory, getHand(buffer));
    }

    public MinigunMagazineMenu(int windowId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.MINIGUN_MAGAZINE.get(), windowId, playerInventory);
        this.hand = hand;

        MinigunItem minigun = (MinigunItem) playerInventory.player.getItemInHand(hand).getItem();
        gunInv = minigun.getMagazine(playerInventory.player.getItemInHand(hand));
        for (int i = 0; i < gunInv.getSlots(); i++) {
            addSlot(new SlotItemHandler(gunInv, i, 26 + (i % 2) * 18, 26 + (i / 2) * 18));
        }

        addPlayerSlots(playerInventory, 84);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);

        gunInv.save();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() == ModItems.MINIGUN.get();
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (clickType == ClickType.CLONE && dragType == 2 && slotId >= 0 && slotId < MinigunItem.MAGAZINE_SIZE) {
            // middle-click to lock a slot
            ItemStack gunStack = player.getItemInHand(hand);
            if (gunStack.getItem() instanceof MinigunItem) {
                int slot = MinigunItem.getLockedSlot(gunStack);
                if (slot == slotId) {
                    gunStack.remove(ModDataComponents.MINIGUN_LOCKED_SLOT);
                } else {
                    gunStack.set(ModDataComponents.MINIGUN_LOCKED_SLOT, slotId);
                }
                if (player.level().isClientSide) {
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                }
            }
        } else {
            super.clicked(slotId, dragType, clickType, player);
            gunInv.save();
        }
    }

    public InteractionHand getHand() {
        return hand;
    }
}
