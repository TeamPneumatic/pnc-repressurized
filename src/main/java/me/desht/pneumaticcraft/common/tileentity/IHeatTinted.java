package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.client.util.TintColor;

@FunctionalInterface
public interface IHeatTinted {
    /**
     * Called clientside to get a tint colour for the given tint index.
     * @param tintIndex the tint index of the model quad to be tinted
     * @return a tint colour
     */
    TintColor getColorForTintIndex(int tintIndex);
}
