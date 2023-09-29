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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public class NightVisionHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {

    @Override
    public ResourceLocation getID() {
        return BuiltinArmorUpgrades.NIGHT_VISION;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.NIGHT_VISION.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return PneumaticValues.PNEUMATIC_NIGHT_VISION_USAGE;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        boolean hasPressure = commonArmorHandler.hasMinPressure(EquipmentSlot.HEAD);
        if (!player.level().isClientSide) {
            MobEffectInstance nvInstance = player.getEffect(MobEffects.NIGHT_VISION);
            if (enabled && hasPressure && (nvInstance == null || nvInstance.getDuration() <= 220)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 500, 0, false, false));
            } else if ((!enabled || !hasPressure) && nvInstance != null) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    @Override
    public void onShutdown(ICommonArmorHandler commonArmorHandler) {
        commonArmorHandler.getPlayer().removeEffect(MobEffects.NIGHT_VISION);
    }
}
