package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class SearchHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    @Override
    public ResourceLocation getID() {
        return RL("search");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.SEARCH };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return PneumaticValues.USAGE_ITEM_SEARCHER;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }
}
