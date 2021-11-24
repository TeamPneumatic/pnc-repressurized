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

package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStandby;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for all pre-programmed (and not programmable) drones.
 * @author MineMaarten
 *
 */
abstract class EntityBasicDrone extends EntityDrone {

    EntityBasicDrone(EntityType<? extends EntityDrone> type, World world, PlayerEntity player) {
        super(type, world, player);
    }

    EntityBasicDrone(EntityType<? extends EntityDrone> type, World world) {
        super(type, world);
    }

    void maybeAddStandbyInstruction(DroneProgramBuilder builder, ItemStack droneStack) {
        if (UpgradableItemUtils.getUpgrades(droneStack, EnumUpgrade.STANDBY) > 0) {
            builder.add(new ProgWidgetStandby());
        }
    }
    
    static ProgWidgetArea standard16x16x16Area(BlockPos centerPos){
        return ProgWidgetArea.fromPositions(centerPos.offset(-16, -16, -16), centerPos.offset(16, 16, 16));
    }
   
}
