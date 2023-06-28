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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ChargingHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final ResourceLocation ID = RL("charging");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.CHARGING.get() };
    }

    @Override
    public int getMaxInstallableUpgrades(PNCUpgrade upgrade) {
        return PneumaticValues.ARMOR_CHARGING_MAX_UPGRADES;
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        if (player.level().isClientSide || !enabled
                || player.level().getGameTime() % PneumaticValues.ARMOR_CHARGER_INTERVAL != 5)
            return;

        int upgrades = commonArmorHandler.getUpgradeCount(EquipmentSlot.CHEST, ModUpgrades.CHARGING.get());
        int airAmount = upgrades * 100 + 100;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot != EquipmentSlot.CHEST) {
                if (!commonArmorHandler.hasMinPressure(EquipmentSlot.CHEST)) return;
                tryPressurize(commonArmorHandler, airAmount, player.getItemBySlot(slot));
            }
        }
        for (ItemStack stack : player.getInventory().items) {
            if (!commonArmorHandler.hasMinPressure(EquipmentSlot.CHEST)) return;
            tryPressurize(commonArmorHandler, airAmount, stack);
        }
    }

    private void tryPressurize(ICommonArmorHandler commonArmorHandler, int airAmount, ItemStack destStack) {
        if (destStack.isEmpty()) return;
        destStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(destHandler -> {
            float pressure = destHandler.getPressure();
            if (pressure < destHandler.maxPressure() && pressure < commonArmorHandler.getArmorPressure(EquipmentSlot.CHEST)) {
                int currentAir = destHandler.getAir();// pressure * destHandler.getVolume();
                int targetAir = (int) (commonArmorHandler.getArmorPressure(EquipmentSlot.CHEST) * destHandler.getVolume());
                int amountToMove = Mth.clamp(targetAir - currentAir, -airAmount, airAmount);
                destHandler.addAir(amountToMove);
                commonArmorHandler.addAir(EquipmentSlot.CHEST, -amountToMove);
            }
        });
    }
}
