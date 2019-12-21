package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiLauncherOptions;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class ChestplateLauncherHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeID() {
        return "launcher";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] { IItemRegistry.EnumUpgrade.DISPENSER.getItem() };
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new GuiLauncherOptions(screen, this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.CHEST;
    }
}
