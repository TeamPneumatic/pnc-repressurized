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

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetLogistics;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetText;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetWait;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class LogisticsDroneEntity extends AbstractBasicDroneEntity {
    public LogisticsDroneEntity(EntityType<LogisticsDroneEntity> type, Level world) {
        super(type, world);
    }

    public LogisticsDroneEntity(Level world, Player player) {
        super(ModEntityTypes.LOGISTICS_DRONE.get(), world, player);
    }

    @Override
    public boolean addProgram(BlockPos clickPos, Direction facing, BlockPos pos, ItemStack droneStack, List<IProgWidget> widgets) {
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(new ProgWidgetLogistics(), standard16x16x16Area(pos));
        builder.add(new ProgWidgetWait(), ProgWidgetText.withText("1s"));  // be kind to server
        maybeAddStandbyInstruction(builder, droneStack);
        widgets.addAll(builder.build());

        return true;
    }
}
