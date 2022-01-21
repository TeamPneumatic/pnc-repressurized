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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;

public class ModulePressureGauge extends TubeModuleRedstoneEmitting {
    public ModulePressureGauge(ItemTubeModule item) {
        super(item);
        lowerBound = 0;
        higherBound = 7.5F;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        pressureTube.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(h -> {
            if (pressureTube.nonNullLevel().getGameTime() % 20 == 0)
                NetworkHandler.sendToAllTracking(new PacketUpdatePressureBlock(getTube(), null, h.getSideLeaking(), h.getAir()), getTube());
            if (setRedstone(getRedstone(h.getPressure()))) {
                // force a recalc on next tick
                pressureTube.tubeModules()
                        .filter(tm -> tm instanceof ModuleRedstone)
                        .forEach(tm -> ((ModuleRedstone) tm).setInputLevel(-1));
            }
        });
    }

    private int getRedstone(float pressure) {
        return (int) ((pressure - lowerBound) / (higherBound - lowerBound) * 15);
    }

    @Override
    public double getWidth() {
        return 8D;
    }

    @Override
    protected double getHeight() {
        return 4D;
    }

    @Override
    public boolean hasGui() {
        return upgraded;
    }
}
