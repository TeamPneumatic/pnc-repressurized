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
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import java.util.Optional;

public class RegulatorModule extends AbstractRedstoneReceivingModule implements IInfluenceDispersing {
    private BlockCapabilityCache<IAirHandlerMachine,Direction> airHandlerCache;

    public RegulatorModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    @Override
    public void onPlaced() {
        super.onPlaced();

        airHandlerCache = pressureTube.getLevel() instanceof ServerLevel level ?
                BlockCapabilityCache.create(PNCCapabilities.AIR_HANDLER_MACHINE, level, pressureTube.getBlockPos().relative(dir), dir.getOpposite(),
                        () -> !pressureTube.isRemoved(), () -> {}) :
                null;
    }

    @Override
    public Item getItem() {
        return ModItems.REGULATOR_TUBE_MODULE.get();
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
    public float getThreshold() {
        // non-upgraded regulator has a simple redstone gradient: 4.9 bar (redstone 0) down to 0 bar (redstone 15)
        return upgraded ? super.getThreshold() : (PneumaticValues.DANGER_PRESSURE_TIER_ONE - 0.1f) * (15 - getReceivingRedstoneLevel()) / 15f;
    }

    private Optional<IAirHandlerMachine> getCachedNeighbourAirHandler() {
        return airHandlerCache == null ? Optional.empty() : Optional.ofNullable(airHandlerCache.getCapability());
    }

    @Override
    public boolean isInlineAndFocused(PressureTubeBlock.TubeHitInfo hitInfo) {
        // regulator module covers its end and the tube center
        return hitInfo.dir() == getDirection() || hitInfo == PressureTubeBlock.TubeHitInfo.CENTER;
    }
}
