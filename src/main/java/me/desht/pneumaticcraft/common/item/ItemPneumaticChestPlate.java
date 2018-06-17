package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import java.util.Set;

@Optional.Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = ModIds.THAUMCRAFT)
public class ItemPneumaticChestPlate extends ItemPneumaticArmorBase {
    public ItemPneumaticChestPlate() {
        super("pneumatic_chestplate", EntityEquipmentSlot.CHEST);
    }

    @Override
    public int getVolume() {
        return PneumaticValues.PNEUMATIC_CHESTPLATE_VOLUME;
    }

    @Override
    public int getMaxAir() {
        return PneumaticValues.PNEUMATIC_CHESTPLATE_MAX_AIR;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        Set<Item> upgrades = super.getApplicableUpgrades();
        upgrades.add(CraftingRegistrator.getUpgrade(IItemRegistry.EnumUpgrade.MAGNET).getItem());
        upgrades.add(CraftingRegistrator.getUpgrade(IItemRegistry.EnumUpgrade.CHARGING).getItem());
        return upgrades;
    }

    @Override
    public int getVisDiscount(ItemStack itemStack, EntityPlayer entityPlayer) {
        return hasThaumcraftUpgradeAndPressure(itemStack) ? 2 : 0;
    }
}
