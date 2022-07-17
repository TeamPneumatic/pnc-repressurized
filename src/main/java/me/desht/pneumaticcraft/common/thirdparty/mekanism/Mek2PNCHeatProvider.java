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

package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerAdapter;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This capability can be attached to Mekanism block entities to make them look like PneumaticCraft heat handlers.
 */
public class Mek2PNCHeatProvider implements ICapabilityProvider {
    private final List<LazyOptional<IHeatExchangerLogic>> handlers = new ArrayList<>();

    private final WeakReference<BlockEntity> teRef;

    public Mek2PNCHeatProvider(BlockEntity te) {
        teRef = new WeakReference<>(te);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        BlockEntity blockEntity = teRef.get();

        if (cap != PNCCapabilities.HEAT_EXCHANGER_CAPABILITY
            || blockEntity == null
            || !blockEntity.getCapability(MekanismIntegration.CAPABILITY_HEAT_HANDLER, side).isPresent()) {
            return LazyOptional.empty();
        }

        if (handlers.isEmpty()) {
            // lazy init of the handlers list; this cap could be attached to any BE so let's not use more memory than necessary
            for (int i = 0; i < 7; i++) {  // 6 faces plus null face
                handlers.add(LazyOptional.empty());
            }
        }

        int idx = side == null ? 6 : side.get3DDataValue();
        if (!handlers.get(idx).isPresent()) {
            LazyOptional<IHeatHandler> heatHandler = blockEntity.getCapability(MekanismIntegration.CAPABILITY_HEAT_HANDLER, side);
            if (heatHandler.isPresent()) {
                heatHandler.addListener(l -> handlers.set(idx, LazyOptional.empty()));
                Mek2PNCHeatAdapter adapter = new Mek2PNCHeatAdapter(side, heatHandler,
                        HeatExchangerLogicAmbient.atPosition(blockEntity.getLevel(), blockEntity.getBlockPos()).getAmbientTemperature(),
                        getResistanceMultiplier(blockEntity));
                handlers.set(idx, LazyOptional.of(() -> adapter));
            }
        }

        //noinspection unchecked
        return (LazyOptional<T>) handlers.get(idx);
    }

    // Mekanism transmitters (i.e. Thermodynamic Conductors or TC's) get special treatment, due to the
    // way Mek handles heat; Mek heater BE's will continue to push heat out to reduce their own
    // temperature back to 300K (Mek ambient), regardless of how hot the sink is.
    // TC's, with a heat capacity of only 1.0, get very hot very fast.
    // This poses a problem for PNC:R, since it handles heat by temperature delta between the two blocks, and
    // TC's will overheat PNC machines really really quickly.  As a kludge, we give TC's a very high thermal
    // resistance when connected to PNC:R blocks, limiting the rate with which a PNC:R heat exchanger will
    // equalise heat directly. This doesn't stop the TC from *pushing* heat, though.
    private double getResistanceMultiplier(BlockEntity te) {
        // FIXME using non-API way of checking this
        if (te instanceof TileEntityTransmitter || te instanceof TileEntityQuantumEntangloporter) {
            return 10000000;
        } else {
            return 1;
        }
    }

    public static class Mek2PNCHeatAdapter extends IHeatExchangerAdapter.Simple<IHeatHandler> {
        private final double thermalResistanceMult;

        public Mek2PNCHeatAdapter(Direction side, LazyOptional<IHeatHandler> heatHandler, double ambientTemperature, double thermalResistanceMult) {
            super(side, heatHandler, ambientTemperature);
            this.thermalResistanceMult = thermalResistanceMult;
        }

        @Override
        public double getTemperature() {
            return foreignHeatCap.map(h -> h.getTemperature(0)).orElse(0d);
        }

        @Override
        public double getThermalResistance() {
            return foreignHeatCap.map(h -> h.getInverseConduction(0) * thermalResistanceMult).orElse(1d);
        }

        @Override
        public double getThermalCapacity() {
            return foreignHeatCap.map(h -> h.getHeatCapacity(0)).orElse(0d);
        }

        @Override
        public void addHeat(double amount) {
            if (amount > 0) {
                // thermal efficiency factor can't be 0 at this point, or adapter caps would not have been added
                foreignHeatCap.ifPresent(h -> h.handleHeat(amount / ConfigHelper.common().integration.mekThermalEfficiencyFactor.get()));
            }
        }
    }
}
