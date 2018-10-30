package me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;

public class ScubaUpgradeHandler extends IUpgradeRenderHandler.SimpleToggleableRenderHandler {
    @Override
    public String getUpgradeName() {
        return "scuba";
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[] {Itemss.upgrades.get(IItemRegistry.EnumUpgrade.SCUBA)};
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.HEAD;
    }
}
