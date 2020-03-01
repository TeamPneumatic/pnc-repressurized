package me.desht.pneumaticcraft.common;

import com.google.common.collect.ImmutableList;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum XPFluidManager {
    INSTANCE;

    public final Map<Fluid, Integer> liquidXPs = new HashMap<>();
    public final List<Fluid> availableLiquidXPs = new ArrayList<>(); // for cycling through xp fluid types

    public static XPFluidManager getInstance() {
        return INSTANCE;
    }

    public void registerXPFluid(Fluid fluid, int liquidToPointRatio) {
        Validate.isTrue(fluid != null && fluid != Fluids.EMPTY, "Fluid may not be null!");
        if (liquidToPointRatio <= 0) {
            liquidXPs.remove(fluid);
            availableLiquidXPs.remove(fluid);
        } else {
            liquidXPs.put(fluid, liquidToPointRatio);
            availableLiquidXPs.add(fluid);
        }
    }

    public int getXPRatio(Fluid fluid) {
        return liquidXPs.getOrDefault(fluid, 0);
    }

    public List<Fluid> getAvailableLiquidXPs() {
        return ImmutableList.copyOf(availableLiquidXPs);
    }
}
