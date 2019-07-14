package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class NightVisionUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeName() {
        return "nightVision";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { ModItems.Registration.UPGRADES.get(IItemRegistry.EnumUpgrade.NIGHT_VISION) };
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }
}
