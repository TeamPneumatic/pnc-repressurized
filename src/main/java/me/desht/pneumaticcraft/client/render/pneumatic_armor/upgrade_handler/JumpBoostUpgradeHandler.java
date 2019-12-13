package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiJumpBoostOptions;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class JumpBoostUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeID() {
        return "jumpBoost";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { EnumUpgrade.RANGE.getItem() };
    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiJumpBoostOptions(this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.LEGS;
    }
}
