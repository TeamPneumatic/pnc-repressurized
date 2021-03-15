package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import net.minecraft.fluid.Fluid;
import net.minecraft.tileentity.TileEntity;

public abstract class HeatBehaviourLiquid extends HeatBehaviour<TileEntity> {
    public Fluid getFluid() {
        if (getBlockState().getFluidState().isEmpty()) return null;
        return getBlockState().getFluidState().getFluid();
    }
}
