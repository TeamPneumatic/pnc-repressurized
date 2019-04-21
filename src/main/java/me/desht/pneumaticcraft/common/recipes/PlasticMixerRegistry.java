package me.desht.pneumaticcraft.common.recipes;

import net.minecraftforge.fluids.Fluid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public enum PlasticMixerRegistry {
    INSTANCE;

    private final Map<String, Integer> fluidNames = new HashMap<>();

    public void registerPlasticMixerInput(Fluid fluid, int ratio) {
        if (ratio > 0) {
            fluidNames.put(fluid.getName(), ratio);
        } else {
            fluidNames.remove(fluid.getName());
        }
    }

    public int getFluidRatio(Fluid fluid) {
        return fluidNames.getOrDefault(fluid.getName(), 0);
    }

    public void clear() {
        fluidNames.clear();
    }

    public Collection<String> getFluids() {
        return fluidNames.keySet();
    }
}
