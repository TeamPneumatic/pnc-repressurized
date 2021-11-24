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

package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ISignEditWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class DroneAIEditSign extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {
    public DroneAIEditSign(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        TileEntity te = drone.world().getBlockEntity(pos);
        if (te instanceof SignTileEntity) {
            SignTileEntity sign = (SignTileEntity) te;
            String[] lines = ((ISignEditWidget) progWidget).getLines();
            for (int i = 0; i < 4; i++) {
                sign.setMessage(i, new StringTextComponent(i < lines.length ? lines[i] : ""));
            }
            BlockState state = drone.world().getBlockState(pos);
            drone.world().sendBlockUpdated(pos, state, state, 3);
        } else if (te instanceof TileEntityAphorismTile) {
            TileEntityAphorismTile sign = (TileEntityAphorismTile) te;
            sign.setTextLines(((ISignEditWidget) progWidget).getLines());
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return false;
    }
}
