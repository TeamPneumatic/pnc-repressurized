package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class StepAssistUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeID() {
        return "stepAssist";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[0];
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.FEET;
    }
}
