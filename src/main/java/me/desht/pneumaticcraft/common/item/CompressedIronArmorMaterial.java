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

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

record CompressedIronArmorMaterial(float knockbackResistance) implements ArmorMaterial {
    private static final int[] DMG_REDUCTION = new int[]{2, 5, 6, 2};
    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return PneumaticValues.PNEUMATIC_ARMOR_DURABILITY_BASE * MAX_DAMAGE_ARRAY[type.getSlot().getIndex()];
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return DMG_REDUCTION[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return 9;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON);
    }

    @Override
    public String getName() {
        return "pneumaticcraft:compressed_iron";
    }

    @Override
    public float getToughness() {
        return 1.0f;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
