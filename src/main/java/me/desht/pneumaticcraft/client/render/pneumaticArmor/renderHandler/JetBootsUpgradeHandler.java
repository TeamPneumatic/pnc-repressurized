package me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;

public class JetBootsUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeName() {
        return "jetBoots";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { Itemss.upgrades.get(IItemRegistry.EnumUpgrade.JET_BOOTS) };
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.FEET;
    }
}
