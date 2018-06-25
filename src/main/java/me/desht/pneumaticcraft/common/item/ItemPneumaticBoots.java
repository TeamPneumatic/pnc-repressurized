package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import java.util.Set;

@Optional.Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = ModIds.THAUMCRAFT)
public class ItemPneumaticBoots extends ItemPneumaticArmorBase {
    public ItemPneumaticBoots() {
        super("pneumatic_boots", EntityEquipmentSlot.FEET);
    }

    @Override
    public int getBaseVolume() {
        return PneumaticValues.PNEUMATIC_BOOTS_VOLUME;
    }

    @Override
    public int getMaxAir() {
        return PneumaticValues.PNEUMATIC_BOOTS_MAX_AIR;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        Set<Item> items = super.getApplicableUpgrades();
        items.add(Itemss.upgrades.get(IItemRegistry.EnumUpgrade.JET_BOOTS));
        return items;
    }

    @Override
    public int getVisDiscount(ItemStack itemStack, EntityPlayer entityPlayer) {
        return 0;
    }
}
