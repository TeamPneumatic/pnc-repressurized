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

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;

public class PressureGaugeModule extends AbstractRedstoneEmittingModule {
    public PressureGaugeModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);

        lowerBound = 0;
        higherBound = 7.5F;
    }

    @Override
    public Item getItem() {
        return ModItems.PRESSURE_GAUGE_MODULE.get();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (pressureTube.nonNullLevel().getGameTime() % 20 == 0) {
            pressureTube.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(h -> {
                NetworkHandler.sendToAllTracking(new PacketUpdatePressureBlock(pressureTube, null, h.getSideLeaking(), h.getAir()), pressureTube);
            });
        }
        if (setRedstone(getRedstone(pressureTube.getPressure()))) {
            // force a recalc on next tick
            pressureTube.tubeModules()
                    .filter(tm -> tm instanceof RedstoneModule)
                    .forEach(tm -> ((RedstoneModule) tm).setInputLevel(-1));
        }

//        pressureTube.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(h -> {
//            if (pressureTube.nonNullLevel().getGameTime() % 20 == 0)
//                NetworkHandler.sendToAllTracking(new PacketUpdatePressureBlock(getTube(), null, h.getSideLeaking(), h.getAir()), getTube());
//            if (setRedstone(getRedstone(h.getPressure()))) {
//                // force a recalc on next tick
//                pressureTube.tubeModules()
//                        .filter(tm -> tm instanceof RedstoneModule)
//                        .forEach(tm -> ((RedstoneModule) tm).setInputLevel(-1));
//            }
//        });
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
