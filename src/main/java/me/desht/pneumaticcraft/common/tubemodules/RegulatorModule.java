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

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

public class RegulatorModule extends AbstractRedstoneReceivingModule implements IInfluenceDispersing, NeighbourAirHandlerCache {
    private LazyOptional<IAirHandlerMachine> neighbourCap;
    private final NonNullConsumer<LazyOptional<IAirHandlerMachine>> neighbourCapInvalidationListener;

    public RegulatorModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);

        this.neighbourCap = LazyOptional.empty();
        this.neighbourCapInvalidationListener = l -> {
            if (l != this.neighbourCap) {
                return;
            }

            neighbourCap = LazyOptional.empty();
        };
    }

    @Override
    public Item getItem() {
        return ModItems.REGULATOR_TUBE_MODULE.get();
    }

    @Override
    public int getMaxDispersion() {
        return getCachedNeighbourAirHandler(pressureTube, dir).map(h -> {
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
        neighbourCap = LazyOptional.empty();
    }

    @Override
    public float getThreshold() {
        // non-upgraded regulator has a simple redstone gradient: 4.9 bar (redstone 0) down to 0 bar (redstone 15)
        return upgraded ? super.getThreshold() : (PneumaticValues.DANGER_PRESSURE_TIER_ONE - 0.1f) * (15 - getReceivingRedstoneLevel()) / 15f;
    }

    @Override
    public boolean isInlineAndFocused(PressureTubeBlock.TubeHitInfo hitInfo) {
        // regulator module covers its end and the tube center
        return hitInfo.dir() == getDirection() || hitInfo == PressureTubeBlock.TubeHitInfo.CENTER;
    }

    @Override
    public LazyOptional<IAirHandlerMachine> getNeighbourCap() {
        return neighbourCap;
    }

    @Override
    public void setNeighbourCap(LazyOptional<IAirHandlerMachine> cap) {
        neighbourCap = cap;
    }

    @Override
    public NonNullConsumer<LazyOptional<IAirHandlerMachine>> getNeighbourCapInvalidationListener() {
        return neighbourCapInvalidationListener;
    }
}
