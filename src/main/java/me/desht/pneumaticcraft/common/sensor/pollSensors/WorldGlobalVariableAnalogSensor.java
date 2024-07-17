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

package me.desht.pneumaticcraft.common.sensor.pollSensors;

import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class WorldGlobalVariableAnalogSensor extends WorldGlobalVariableSensor {
    @Override
    public String getSensorPath() {
        return "World/Global Analog Var.";
    }

    @Override
    public int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText) {
        return Mth.clamp(GlobalVariableHelper.getInstance().getInt(playerID, textBoxText), 0, 15);
    }
}
