package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.PNCConfig.Common.Integration;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.ISidedHeatHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This capability object can be attached to PneumaticCraft heat-handling tile entities to make them look like
 * Mekanism heat handlers.
 */
public class PNC2MekHeatProvider implements ICapabilityProvider {
    private final List<LazyOptional<IHeatHandler>> handlers = new ArrayList<>();

    private final WeakReference<TileEntity> teRef;

    public PNC2MekHeatProvider(TileEntity te) {
        teRef = new WeakReference<>(te);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap != Mekanism.CAPABILITY_HEAT_HANDLER
                || teRef.get() == null
                || !teRef.get().getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, side).isPresent())
        {
            return LazyOptional.empty();
        }

        if (handlers.isEmpty()) {
            // lazy init of the handlers list; this cap could be attached to any TE
            for (int i = 0; i < 7; i++) {  // 6 faces plus null face
                handlers.add(LazyOptional.empty());
            }
        }

        int idx = side == null ? 6 : side.getIndex();
        if (!handlers.get(idx).isPresent()) {
            LazyOptional<IHeatExchangerLogic> heatExchanger = teRef.get().getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, side);
            if (heatExchanger.isPresent()) {
                heatExchanger.addListener(l -> handlers.set(idx, LazyOptional.empty()));
                PNC2MekHeatAdapter adapter = new PNC2MekHeatAdapter(side, heatExchanger);
                handlers.set(idx, LazyOptional.of(() -> adapter));
            }
        }

        //noinspection unchecked
        return (LazyOptional<T>) handlers.get(idx);
    }

    public static class PNC2MekHeatAdapter implements ISidedHeatHandler {
        private final Direction side;
        private final LazyOptional<IHeatExchangerLogic> heatExchanger;

        public PNC2MekHeatAdapter(Direction side, LazyOptional<IHeatExchangerLogic> heatExchanger) {
            this.side = side;
            this.heatExchanger = heatExchanger;
        }

        @Nullable
        @Override
        public Direction getHeatSideFor() {
            return side;
        }

        @Override
        public int getHeatCapacitorCount(@Nullable Direction direction) {
            return heatExchanger.isPresent() ? 1 : 0;
        }

        @Override
        public double getTemperature(int i, @Nullable Direction direction) {
            return heatExchanger.map(IHeatExchangerLogic::getTemperature).orElse(0d);
        }

        @Override
        public double getInverseConduction(int i, @Nullable Direction direction) {
            return heatExchanger.map(h -> h.getThermalResistance() * Integration.mekThermalResistanceFactor).orElse(1d);
        }

        @Override
        public double getHeatCapacity(int i, @Nullable Direction direction) {
            return heatExchanger.map(IHeatExchangerLogic::getThermalCapacity).orElse(0d);
        }

        @Override
        public void handleHeat(int i, double amount, @Nullable Direction direction) {
                heatExchanger.ifPresent(h -> h.addHeat(amount * Integration.mekThermalEfficiencyFactor));
        }
    }
}
