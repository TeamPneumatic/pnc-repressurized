/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.BuiltinArmorUpgrades;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class EntityTrackerHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final float ENTITY_TRACKING_RANGE = 16F;

    @Override
    public ResourceLocation getID() {
        return BuiltinArmorUpgrades.ENTITY_TRACKER;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.ENTITY_TRACKER.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        int upgrades = armorHandler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.ENTITY_TRACKER.get());
        return PneumaticValues.USAGE_ENTITY_TRACKER * (1 + (float) Math.min(10, upgrades)
                * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / ENTITY_TRACKING_RANGE);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}
