package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.PNCConfig.Common.Integration;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * This capability can be attached to Mekanism tile entities to make them look like PneumaticCraft heat handlers.
 */
public class Mek2PNCHeatProvider implements ICapabilityProvider {
    private final List<LazyOptional<IHeatExchangerLogic>> handlers = new ArrayList<>();

    private final WeakReference<TileEntity> teRef;

    public Mek2PNCHeatProvider(TileEntity te) {
        teRef = new WeakReference<>(te);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap != PNCCapabilities.HEAT_EXCHANGER_CAPABILITY
            || teRef.get() == null
            || !teRef.get().getCapability(MekanismIntegration.CAPABILITY_HEAT_HANDLER, side).isPresent()) {
            return LazyOptional.empty();
        }

        if (handlers.isEmpty()) {
            // lazy init of the handlers list; this cap could be attached to any TE so let's not use more memory than necessary
            for (int i = 0; i < 7; i++) {  // 6 faces plus null face
                handlers.add(LazyOptional.empty());
            }
        }

        int idx = side == null ? 6 : side.getIndex();
        if (!handlers.get(idx).isPresent()) {
            TileEntity te = teRef.get();
            LazyOptional<IHeatHandler> heatHandler = te.getCapability(MekanismIntegration.CAPABILITY_HEAT_HANDLER, side);
            if (heatHandler.isPresent()) {
                heatHandler.addListener(l -> handlers.set(idx, LazyOptional.empty()));
                Mek2PNCHeatAdapter adapter = new Mek2PNCHeatAdapter(side, heatHandler,
                        HeatExchangerLogicAmbient.atPosition(te.getWorld(), te.getPos()).getAmbientTemperature(),
                        getResistanceMultiplier(te));
                handlers.set(idx, LazyOptional.of(() -> adapter));
            }
        }

        //noinspection unchecked
        return (LazyOptional<T>) handlers.get(idx);
    }

    // Mekanism transmitters (i.e. Thermodynamic Conductors or TC's) get special treatment, due to the
    // way Mek handles heat; Mek heater TE's will continue to push heat out to reduce their own
    // temperature back to 300K (Mek ambient), regardless of how hot the sink is.
    // TC's, with a heat capacity of only 1.0, get very hot very fast.
    // This poses a problem for PNC:R, since it handles heat by temperature delta between the two blocks, and
    // TC's will overheat PNC machines really really quickly.  As a kludge, we give TC's a very high thermal
    // resistance when connected to PNC:R blocks, limiting the rate with which a PNC:R heat exchanger will
    // equalise heat directly. This doesn't stop the TC from *pushing* heat, though.
    private double getResistanceMultiplier(TileEntity te) {
        // FIXME using non-API way of checking this
        if (te instanceof TileEntityTransmitter || te instanceof TileEntityQuantumEntangloporter) {
            return 10000000;
        } else {
            return 1;
        }
    }

    public static class Mek2PNCHeatAdapter implements IHeatExchangerLogic {
        private final Direction side;
        private final LazyOptional<IHeatHandler> heatHandler;
        private final double ambientTemperature;
        private final double thermalResistanceMult;

        public Mek2PNCHeatAdapter(Direction side, LazyOptional<IHeatHandler> heatHandler, double ambientTemperature, double thermalResistanceMult) {
            this.side = side;
            this.heatHandler = heatHandler;
            this.ambientTemperature = ambientTemperature;
            this.thermalResistanceMult = thermalResistanceMult;
        }

        @Override
        public void tick() {
        }

        @Override
        public void initializeAsHull(World world, BlockPos pos, BiPredicate<IWorld, BlockPos> blockFilter, Direction... validSides) {
        }

        @Override
        public void initializeAmbientTemperature(World world, BlockPos pos) {
        }

        @Override
        public void addConnectedExchanger(IHeatExchangerLogic exchanger) {
        }

        @Override
        public void removeConnectedExchanger(IHeatExchangerLogic exchanger) {
        }

        @Override
        public void setTemperature(double temperature) {

        }

        @Override
        public double getTemperature() {
            return heatHandler.map(h -> h.getTemperature(0)).orElse(0d);
        }

        @Override
        public int getTemperatureAsInt() {
            return (int) getTemperature();
        }

        @Override
        public double getAmbientTemperature() {
            return ambientTemperature;
        }

        @Override
        public void setThermalResistance(double thermalResistance) {
        }

        @Override
        public double getThermalResistance() {
            return heatHandler.map(h -> h.getInverseConduction(0) * thermalResistanceMult).orElse(1d);
        }

        @Override
        public void setThermalCapacity(double capacity) {
        }

        @Override
        public double getThermalCapacity() {
            return heatHandler.map(h -> h.getHeatCapacity(0)).orElse(0d);
        }

        @Override
        public void addHeat(double amount) {
            if (amount > 0) {
                // thermal efficiency factor can't be 0 at this point, or adapter caps would not have been added
                heatHandler.ifPresent(h -> h.handleHeat(amount / Integration.mekThermalEfficiencyFactor));
            }
        }

        @Override
        public boolean isSideConnected(Direction side) {
            return side == this.side;
        }

        @Override
        public CompoundNBT serializeNBT() {
            return null;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
        }
    }
}
