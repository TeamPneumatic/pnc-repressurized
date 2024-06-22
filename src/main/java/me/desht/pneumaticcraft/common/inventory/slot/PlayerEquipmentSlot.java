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

package me.desht.pneumaticcraft.common.inventory.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class PlayerEquipmentSlot extends Slot {
    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
    };
    private final EquipmentSlot slotType;

    public PlayerEquipmentSlot(Inventory inventoryIn, EquipmentSlot slotType, int xPosition, int yPosition) {
        super(inventoryIn, getIndexForSlot(slotType), xPosition, yPosition);
        this.slotType = slotType;
    }

    @Override
    public int getMaxStackSize() {
        return slotType == EquipmentSlot.OFFHAND ? super.getMaxStackSize() : 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return slotType == EquipmentSlot.OFFHAND ? super.mayPlace(stack) : stack.canEquip(slotType, ((Inventory) container).player);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        if (slotType == EquipmentSlot.OFFHAND) return super.mayPickup(playerIn);

        ItemStack itemstack = this.getItem();
        return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(playerIn);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return slotType.getType() == EquipmentSlot.Type.HUMANOID_ARMOR ?
                Pair.of(InventoryMenu.BLOCK_ATLAS, ARMOR_SLOT_TEXTURES[slotType.getIndex()]) :
                Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
    }

    private static int getIndexForSlot(EquipmentSlot type) {
        if (type.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            return 36 + type.getIndex();
        } else if (type == EquipmentSlot.OFFHAND) {
            return 40;
        } else {
            throw new IllegalArgumentException("invalid equipment slot: " + type);
        }
    }
}
