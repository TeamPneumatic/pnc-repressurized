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
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

public class RegulatorModule extends AbstractRedstoneReceivingModule implements IInfluenceDispersing {
    public static boolean hasTicked;
    public static boolean inLine;
    public static boolean inverted;

    private LazyOptional<IAirHandlerMachine> neighbourCap = null;

    public RegulatorModule(ItemTubeModule itemTubeModule) {
        super(itemTubeModule);
    }

    @Override
    public int getMaxDispersion() {
        return getCachedNeighbourAirHandler().map(h -> {
            int maxDispersion = (int) ((getThreshold() - h.getPressure()) * h.getVolume());
            return Math.max(0, maxDispersion);
        }).orElse(0);
    }

    @Override
    public void onAirDispersion(int amount) {
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        neighbourCap = null;
    }

    @Override
    public float getThreshold() {
        // non-upgraded regulator has a simple redstone gradient: 4.9 bar (redstone 0) down to 0 bar (redstone 15)
        return upgraded ? super.getThreshold() : (PneumaticValues.DANGER_PRESSURE_TIER_ONE - 0.1f) * (15 - getReceivingRedstoneLevel()) / 15f;
    }

    private LazyOptional<IAirHandlerMachine> getCachedNeighbourAirHandler() {
        if (neighbourCap == null) {
            BlockEntity neighborTE = pressureTube.nonNullLevel().getBlockEntity(pressureTube.getBlockPos().relative(dir));
            if (neighborTE != null) {
                neighbourCap = neighborTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite());
                if (neighbourCap.isPresent()) neighbourCap.addListener(l -> neighbourCap = null);
            } else {
                neighbourCap = LazyOptional.empty();
            }
        }
        return neighbourCap;
    }
}
