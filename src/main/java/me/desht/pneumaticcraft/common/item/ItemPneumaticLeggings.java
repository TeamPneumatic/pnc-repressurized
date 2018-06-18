package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import java.util.Set;

@Optional.Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = ModIds.THAUMCRAFT)
public class ItemPneumaticLeggings extends ItemPneumaticArmorBase {
    public ItemPneumaticLeggings() {
        super("pneumatic_leggings", EntityEquipmentSlot.LEGS);
    }

    @Override
    public int getBaseVolume() {
        return PneumaticValues.PNEUMATIC_LEGGINGS_VOLUME;
    }

    @Override
    public int getMaxAir() {
        return PneumaticValues.PNEUMATIC_LEGGINGS_MAX_AIR;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        return super.getApplicableUpgrades(); // TODO
    }

    @Override
    public int getVisDiscount(ItemStack itemStack, EntityPlayer entityPlayer) {
        return 0;
    }
}
