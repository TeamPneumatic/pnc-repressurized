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

package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

public class TANModifierAirConditioning /*implements ITemperatureModifier*/ {
//    private static Map<UUID, Integer> lastDelta = new HashMap<>();
//
//    @Override
//    public Temperature applyEnvironmentModifiers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
//        return initialTemperature;
//    }
//
//    @Override
//    public Temperature applyPlayerModifiers(@Nonnull PlayerEntity player, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
//        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
//
//        if (!handler.isAirConEnabled()
//                || !handler.isArmorReady(EquipmentSlotType.CHEST)
//                || handler.getArmorPressure(EquipmentSlotType.CHEST) < 0.1) {
//            return initialTemperature;
//        }
//        int upgrades = handler.getUpgradeCount(EquipmentSlotType.CHEST, IItemRegistry.EnumUpgrade.AIR_CONDITIONING, 4);
//        if (upgrades == 0) {
//            return initialTemperature;
//        }
//
//        int targetTemp = initialTemperature.getRawValue();
//        int playerTemp = TemperatureHelper.getTemperatureData(player).getTemperature().getRawValue();
//        int deltaTemp = (TemperatureScale.getScaleMidpoint() - playerTemp);
//        if (Math.abs(deltaTemp) < 2)
//            deltaTemp = 0;
//        else if (Math.abs(deltaTemp) == 2)
//            deltaTemp /= 2;
//
//        deltaTemp *= upgrades;
//        targetTemp += deltaTemp;
//        if (deltaTemp != lastDelta.getOrDefault(player.getUniqueID(), 0)) {
//            NetworkHandler.sendToPlayer(new PacketPlayerTemperatureDelta(deltaTemp), (ServerPlayerEntity) player);
//            lastDelta.put(player.getUniqueID(), deltaTemp);
//        }
//
//        int airUsage = (int) (deltaTemp * ConfigHandler.integration.tanAirConAirUsageMultiplier);
//        handler.addAir(EquipmentSlotType.CHEST, -Math.abs(airUsage));
//
//        Temperature res = new Temperature(targetTemp);
//        monitor.addEntry(new IModifierMonitor.Context(this.getId(), "Pneumatic Armor A/C", initialTemperature, res));
//        return res;
//    }
//
//    @Override
//    public boolean isPlayerSpecific() {
//        return true;
//    }
//
//    @Nonnull
//    @Override
//    public String getId() {
//        return "pneumaticcraft:air_conditioning";
//    }
}
