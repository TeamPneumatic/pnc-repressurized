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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.block.entity.utility.AphorismTileBlockEntity;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.ISignEditWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;

public class DroneAIEditSign extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {
    public DroneAIEditSign(IDrone drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (progWidget instanceof ISignEditWidget signEditWidget) {
            BlockEntity te = drone.getDroneLevel().getBlockEntity(pos);
            if (te instanceof SignBlockEntity sign) {
                String[] lines = signEditWidget.getLines();
                SignText signText = signEditWidget.isSignBackSide() ? sign.getBackText() : sign.getFrontText();
                for (int i = 0; i < 4; i++) {
                    signText.setMessage(i, Component.literal(i < lines.length ? lines[i] : ""));
                }
                BlockState state = drone.getDroneLevel().getBlockState(pos);
                drone.getDroneLevel().sendBlockUpdated(pos, state, state, 3);
            } else if (te instanceof AphorismTileBlockEntity teAT) {
                teAT.setTextLines(signEditWidget.getLines());
            }
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return false;
    }
}
