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

package me.desht.pneumaticcraft.common.entity.drone;

import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetStandby;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Base class for all pre-programmed (and not programmable) drones.
 * @author MineMaarten
 *
 */
abstract class AbstractBasicDroneEntity extends DroneEntity {

    AbstractBasicDroneEntity(EntityType<? extends DroneEntity> type, Level world, Player player) {
        super(type, world, player);
    }

    AbstractBasicDroneEntity(EntityType<? extends DroneEntity> type, Level world) {
        super(type, world);
    }

    void maybeAddStandbyInstruction(DroneProgramBuilder builder, ItemStack droneStack) {
        if (UpgradableItemUtils.getUpgradeCount(droneStack, ModUpgrades.STANDBY.get()) > 0) {
            builder.add(new ProgWidgetStandby());
        }
    }
    
    static ProgWidgetArea standard16x16x16Area(BlockPos centerPos){
        return ProgWidgetArea.fromPositions(centerPos.offset(-16, -16, -16), centerPos.offset(16, 16, 16));
    }
   
}
