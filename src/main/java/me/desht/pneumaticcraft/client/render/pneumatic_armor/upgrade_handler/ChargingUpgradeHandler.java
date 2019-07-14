package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class ChargingUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeName() {
        return "charging";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { IItemRegistry.EnumUpgrade.CHARGING.getItem() };
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.CHEST;
    }
}
