package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public enum PneumaticArmorLayerColors implements IClientItemExtensions {
    INSTANCE;

    @Override
    public int getArmorLayerTintColor(ItemStack stack, LivingEntity entity, ArmorMaterial.Layer layer, int layerIdx, int fallbackColor) {
        if (stack.getItem() instanceof PneumaticArmorItem armor) {
            return switch (layerIdx) {
                case 0 -> armor.getPrimaryColor(stack);
                case 1 -> armor.getSecondaryColor(stack);
                case 2 -> armor.getEquipmentSlot() == EquipmentSlot.HEAD ? armor.getEyepieceColor(stack) : 0x0;
                case 3 -> armor.getEquipmentSlot() == EquipmentSlot.CHEST ? 0xFFFFFFFF : 0x0;
                default -> 0xFFFFFFFF;
            };
        }
        return fallbackColor;
    }

    @Override
    public int getDefaultDyeColor(ItemStack stack) {
        return 0xFFFFFFFF;
    }
}
