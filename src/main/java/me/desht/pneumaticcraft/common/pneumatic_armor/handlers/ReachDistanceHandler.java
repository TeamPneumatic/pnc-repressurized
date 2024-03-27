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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.UUID;

public class ReachDistanceHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final UUID REACH_DIST_BOOST_ID = UUID.fromString("c9dce729-70c4-4c0f-95d4-31d2e50bc826");
    public static final AttributeModifier REACH_DIST_BOOST = new AttributeModifier(REACH_DIST_BOOST_ID, "Pneumatic Reach Boost", 3.5D, AttributeModifier.Operation.ADDITION);

    @Override
    public ResourceLocation getID() {
        return BuiltinArmorUpgrades.REACH_DISTANCE;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.RANGE.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 5;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        if ((player.level().getGameTime() & 0xf) == 0) {
            AttributeInstance attr = player.getAttribute(NeoForgeMod.BLOCK_REACH.value());
            if (attr != null) {
                attr.removeModifier(REACH_DIST_BOOST_ID);
                if (enabled && commonArmorHandler.hasMinPressure(EquipmentSlot.CHEST) && commonArmorHandler.isArmorEnabled()) {
                    attr.addTransientModifier(REACH_DIST_BOOST);
                }
            }
        }
    }

    @Override
    public void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
        if (!newState) {
            AttributeInstance attr = commonArmorHandler.getPlayer().getAttribute(NeoForgeMod.BLOCK_REACH.value());
            if (attr != null) {
                attr.removeModifier(ReachDistanceHandler.REACH_DIST_BOOST_ID);
            }
        }
    }
}
