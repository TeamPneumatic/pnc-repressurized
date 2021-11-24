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

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class SpeedBoostHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final Vector3d FORWARD = new Vector3d(0, 0, 1);

    // track player movement across ticks on the server - very transient, a capability would be overkill here
    private static final Map<UUID,Vector3d> moveMap = new HashMap<>();

    @Override
    public ResourceLocation getID() {
        return RL("run_speed");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.SPEED };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.LEGS;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        PlayerEntity player = commonArmorHandler.getPlayer();

        double speedBoost = getSpeedBoostFromLegs(commonArmorHandler);
        if (speedBoost == 0) return;

        if (player.level.isClientSide) {
            // doing this client-side only appears to be effective
            if (player.zza > 0) {
                JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getClientTracker().getJetBootsState(player);
                if (!player.isOnGround() && jbState.isEnabled() && jbState.isBuilderMode()) {
                    player.moveRelative(commonArmorHandler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) / 250f, FORWARD);
                }
                if (player.isOnGround() && !player.isInWater()) {
                    player.moveRelative((float) speedBoost, FORWARD);
                }
            }
        }
        if (!player.level.isClientSide && speedBoost > 0) {
            Vector3d prev = moveMap.get(player.getUUID());
            boolean moved = prev != null && (Math.abs(player.getX() - prev.x) > 0.0001 || Math.abs(player.getZ() - prev.z) > 0.0001);
            if (moved && player.isOnGround() && !player.isInWater()) {
                int airUsage = (int) Math.ceil(PneumaticValues.PNEUMATIC_LEGS_SPEED_USAGE * speedBoost * 8);
                commonArmorHandler.addAir(EquipmentSlotType.LEGS, -airUsage);
            }
            moveMap.put(player.getUUID(), player.position());
        }
    }

    public double getSpeedBoostFromLegs(ICommonArmorHandler commonArmorHandler) {
        if (commonArmorHandler.upgradeUsable(ArmorUpgradeRegistry.getInstance().runSpeedHandler, true)) {
            int speedUpgrades = commonArmorHandler.getUpgradeCount(EquipmentSlotType.LEGS, EnumUpgrade.SPEED);
            PlayerEntity player = commonArmorHandler.getPlayer();
            ItemStack armorStack = player.getItemBySlot(EquipmentSlotType.LEGS);
            float speedBoostMult = ItemPneumaticArmor.getIntData(armorStack, ItemPneumaticArmor.NBT_SPEED_BOOST, 100, 0, 100) / 100f;
            return PneumaticValues.PNEUMATIC_LEGS_BOOST_PER_UPGRADE * speedUpgrades * speedBoostMult;
        } else {
            return 0.0;
        }
    }
}
