package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

class CompressedIronArmorMaterial implements IArmorMaterial {
    static final int[] DMG_REDUCTION = new int[]{2, 5, 6, 2};
    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    private final float knockbackResistance;

    public CompressedIronArmorMaterial(float knockbackResistance) {
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlotType equipmentSlotType) {
        return PneumaticValues.PNEUMATIC_ARMOR_DURABILITY_BASE * MAX_DAMAGE_ARRAY[equipmentSlotType.getIndex()];
    }

    @Override
    public int getDefenseForSlot(EquipmentSlotType equipmentSlotType) {
        return DMG_REDUCTION[equipmentSlotType.getIndex()];
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
