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
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

public class VacuumModule extends AbstractRedstoneReceivingModule implements IInfluenceDispersing {
    private LazyOptional<IAirHandlerMachine> neighbourCap;
    private final NonNullConsumer<LazyOptional<IAirHandlerMachine>> neighbourCapInvalidationListener;

    public float rotation, oldRotation;
    private int lastAmount = 0;

    public VacuumModule(Direction dir, PressureTubeBlockEntity pressureTube) {
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
        return ModItems.VACUUM_MODULE.get();
    }

    @Override
    public void tickCommon() {
        oldRotation = rotation;
        rotation += lastAmount / 5F;
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();

        neighbourCap = LazyOptional.empty();
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

    private LazyOptional<IAirHandlerMachine> getCachedNeighbourAirHandler() {
        if (!neighbourCap.isPresent()) {
            BlockEntity neighborTE = pressureTube.nonNullLevel().getBlockEntity(pressureTube.getBlockPos().relative(dir));
            if (neighborTE != null) {
                LazyOptional<IAirHandlerMachine> cap = neighborTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite());
                if (cap.isPresent()) {
                    neighbourCap = cap;
                    cap.addListener(this.neighbourCapInvalidationListener);
                }
            } else {
                neighbourCap = LazyOptional.empty();
            }
        }
        return neighbourCap;
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
