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
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunSpeedHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final Vec3 FORWARD = new Vec3(0, 0, 1);

    // track player movement across ticks on the server - very transient, a capability would be overkill here
    private static final Map<UUID,Vec3> MOVE_MAP = new HashMap<>();

    @Override
    public ResourceLocation getID() {
        return BuiltinArmorUpgrades.RUN_SPEED;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.SPEED.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.LEGS;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();

        double speedBoost = getSpeedBoostFromLegs(commonArmorHandler);
        if (speedBoost == 0) return;

        if (player.level().isClientSide) {
            // doing this client-side only appears to be effective
            if (player.zza > 0) {
                JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getClientTracker().getJetBootsState(player);
                if (!player.onGround() && jbState.isEnabled() && jbState.isBuilderMode()) {
                    player.moveRelative(commonArmorHandler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get()) / 250f, FORWARD);
                }
                if (player.onGround() && !player.isInWater()) {
                    player.moveRelative((float) speedBoost, FORWARD);
                }
            }
        }
        if (!player.level().isClientSide && speedBoost > 0) {
            Vec3 prev = MOVE_MAP.get(player.getUUID());
            boolean moved = prev != null && (Math.abs(player.getX() - prev.x) > 0.0001 || Math.abs(player.getZ() - prev.z) > 0.0001);
            if (moved && player.onGround() && !player.isInWater()) {
                int airUsage = (int) Math.ceil(PneumaticValues.PNEUMATIC_LEGS_SPEED_USAGE * speedBoost * 8);
                commonArmorHandler.addAir(EquipmentSlot.LEGS, -airUsage);
            }
            MOVE_MAP.put(player.getUUID(), player.position());
        }
    }

    public double getSpeedBoostFromLegs(ICommonArmorHandler commonArmorHandler) {
        if (commonArmorHandler.upgradeUsable(CommonUpgradeHandlers.runSpeedHandler, true)) {
            int speedUpgrades = commonArmorHandler.getUpgradeCount(EquipmentSlot.LEGS, ModUpgrades.SPEED.get());
            Player player = commonArmorHandler.getPlayer();
            ItemStack armorStack = player.getItemBySlot(EquipmentSlot.LEGS);
            float speedBoostMult = PneumaticArmorItem.getIntData(armorStack, PneumaticArmorItem.NBT_SPEED_BOOST, 100, 0, 100) / 100f;
            return PneumaticValues.PNEUMATIC_LEGS_BOOST_PER_UPGRADE * speedUpgrades * speedBoostMult;
        } else {
            return 0.0;
        }
    }
}
