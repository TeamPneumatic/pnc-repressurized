package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiKickOptions;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class KickUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeName() {
        return "kick";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { EnumUpgrade.DISPENSER.getItem() };
    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiKickOptions();
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.FEET;
    }
}
