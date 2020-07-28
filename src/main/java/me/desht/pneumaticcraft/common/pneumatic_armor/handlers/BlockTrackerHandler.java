package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class BlockTrackerHandler implements IArmorUpgradeHandler {
    public static final int BLOCK_TRACKING_RANGE = 30;

    @Override
    public ResourceLocation getID() {
        return RL("block_tracker");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.BLOCK_TRACKER };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        int upgrades = armorHandler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.BLOCK_TRACKER);
        return PneumaticValues.USAGE_BLOCK_TRACKER
                * (1 + (float) Math.min(5, upgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / BLOCK_TRACKING_RANGE);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }
}
