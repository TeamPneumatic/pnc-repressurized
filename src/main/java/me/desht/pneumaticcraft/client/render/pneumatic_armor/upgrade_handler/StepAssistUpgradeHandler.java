package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;

public class StepAssistUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeName() {
        return "stepAssist";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[0];
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.FEET;
    }
}
