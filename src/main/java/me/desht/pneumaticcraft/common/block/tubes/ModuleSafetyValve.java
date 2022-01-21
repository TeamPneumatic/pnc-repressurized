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

package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.lib.PneumaticValues;

public class ModuleSafetyValve extends TubeModuleRedstoneReceiving {

    public ModuleSafetyValve(ItemTubeModule item) {
        super(item);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (pressureTube.getPressure() > getThreshold()) {
            pressureTube.forceLeak(dir);
        }
    }

    @Override
    public float getThreshold() {
        if (upgraded) return super.getThreshold();

        // 4.92 instead of 4.9 because if the system is being fed via regulator from a high pressure line,
        // then it will be at 4.9 bar, which would cause safety modules to leak unnecessarily...
        return getTube().getDangerPressure() == PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE ? 19.9f : 4.92f;
    }
}
