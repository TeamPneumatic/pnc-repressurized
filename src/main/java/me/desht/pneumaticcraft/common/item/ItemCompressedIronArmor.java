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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class ItemCompressedIronArmor extends ArmorItem {
    private static final IArmorMaterial COMPRESSED_IRON_ARMOR_MATERIAL = new CompressedIronArmorMaterial(0.075f);

    public ItemCompressedIronArmor(EquipmentSlotType slot) {
        super(COMPRESSED_IRON_ARMOR_MATERIAL, slot, ModItems.defaultProps());
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return slot == EquipmentSlotType.LEGS ? Textures.ARMOR_COMPRESSED_IRON + "_2.png" : Textures.ARMOR_COMPRESSED_IRON + "_1.png";
    }
}
