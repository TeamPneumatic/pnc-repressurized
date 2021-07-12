package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ChestplateLauncherHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    @Override
    public ResourceLocation getID() {
        return RL("chestplate_launcher");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.DISPENSER };
    }

    @Override
    public int getMaxInstallableUpgrades(EnumUpgrade upgrade) {
        return 4;
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.CHEST;
    }
}
