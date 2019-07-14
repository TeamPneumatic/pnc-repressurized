package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class ScubaUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeName() {
        return "scuba";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { EnumUpgrade.SCUBA.getItem() };
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }
}
