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
