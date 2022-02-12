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

import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerMinigunMagazine extends AbstractPneumaticCraftMenu<TileEntityBase> {
    private final ItemMinigun.MagazineHandler gunInv;
    private final InteractionHand hand;

    public ContainerMinigunMagazine(int i, Inventory playerInventory, @SuppressWarnings("unused") FriendlyByteBuf buffer) {
        this(i, playerInventory, getHand(buffer));
    }

    public ContainerMinigunMagazine(int windowId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.MINIGUN_MAGAZINE.get(), windowId, playerInventory);
        this.hand = hand;

        ItemMinigun minigun = (ItemMinigun) playerInventory.player.getItemInHand(hand).getItem();
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
        return true;
    }

    @Nonnull
    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (clickType == ClickType.CLONE && dragType == 2 && slotId >= 0 && slotId < ItemMinigun.MAGAZINE_SIZE) {
            // middle-click to lock a slot
            ItemStack gunStack = player.getItemInHand(hand);
            if (gunStack.getItem() instanceof ItemMinigun) {
                int slot = ItemMinigun.getLockedSlot(gunStack);
                if (slot == slotId) {
                    NBTUtils.removeTag(gunStack, ItemMinigun.NBT_LOCKED_SLOT);
                } else {
                    NBTUtils.setInteger(gunStack, ItemMinigun.NBT_LOCKED_SLOT, slotId);
                }
                if (player.level.isClientSide) {
                    player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1.0f);
                }
            }
        } else {
            super.clicked(slotId, dragType, clickType, player);
        }
    }

    public InteractionHand getHand() {
        return hand;
    }
}
