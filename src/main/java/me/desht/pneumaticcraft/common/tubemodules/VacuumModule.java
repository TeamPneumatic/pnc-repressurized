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
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import java.util.Optional;

public class VacuumModule extends AbstractRedstoneReceivingModule implements IInfluenceDispersing {
    //    private LazyOptional<IAirHandlerMachine> neighbourCap = null;
    private BlockCapabilityCache<IAirHandlerMachine,Direction> airHandlerCache;

    public float rotation, oldRotation;
    private int lastAmount = 0;

    public VacuumModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);

    }

    @Override
    public Item getItem() {
        return ModItems.VACUUM_MODULE.get();
    }

    @Override
    public void tickCommon() {
        oldRotation = rotation;
        rotation += lastAmount / 5F;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        int prevLast = lastAmount;
        PressureTubeBlockEntity tube = getTube();
        if (tube.getPressure() >= PneumaticValues.MIN_PRESSURE_VACUUM_PUMP && getCachedNeighbourAirHandler().isPresent() && getReceivingRedstoneLevel() == 0) {
            int toAdd = (int) (-PneumaticValues.USAGE_VACUUM_PUMP * (upgraded ? 7.41f : 1));
            int toTake = (int) (-PneumaticValues.PRODUCTION_VACUUM_PUMP * (upgraded ? 5.06f : 1));

            lastAmount = getCachedNeighbourAirHandler().map(h -> {
                int air = h.getAir();
                float pressure = h.getPressure();
                h.addAir(toTake);
                return PneumaticCraftUtils.epsilonEquals(pressure, h.getPressure()) ? 0 : air - h.getAir();
            }).orElse(0);

            if (lastAmount != 0) {
                tube.addAir(toAdd * -lastAmount / toTake);
            }
        } else {
            lastAmount = 0;
        }

        if (prevLast != lastAmount) {
            tube.sendDescriptionPacket();
        }
    }

    @Override
    public boolean canUpgrade() {
        return true;
    }

    @Override
    public boolean isInline() {
        return true;
    }

    public int getLastAmount() {
        return lastAmount;
    }

    public void setLastAmount(int lastAmount) {
        this.lastAmount = lastAmount;
    }

    private Optional<IAirHandlerMachine> getCachedNeighbourAirHandler() {
        if (airHandlerCache == null && pressureTube.getLevel() instanceof ServerLevel level) {
            airHandlerCache = BlockCapabilityCache.create(PNCCapabilities.AIR_HANDLER_MACHINE, level, pressureTube.getBlockPos().relative(dir), dir.getOpposite(),
                    () -> !pressureTube.isRemoved(), () -> airHandlerCache = null);
        }
        return airHandlerCache == null ? Optional.empty() : Optional.ofNullable(airHandlerCache.getCapability());
    }

    @Override
    public int getMaxDispersion() {
        // allow no air through
        return 0;
    }

    @Override
    public void onAirDispersion(int amount) {
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        lastAmount = tag.getInt("lastAmount");
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("lastAmount", lastAmount);
        return super.writeToNBT(tag);
    }

    @Override
    public boolean isInlineAndFocused(PressureTubeBlock.TubeHitInfo hitInfo) {
        // vacuum module is large and covers entire tube
        return true;
    }
}
