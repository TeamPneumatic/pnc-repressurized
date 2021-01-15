package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ILiquidFiltered;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class DroneAIVoidLiquid extends Goal {
    private final IDroneBase drone;
    private final ILiquidFiltered widget;

    public DroneAIVoidLiquid(IDroneBase drone, ILiquidFiltered widget) {
        this.drone = drone;
        this.widget = widget;
    }

    @Override
    public boolean shouldExecute() {
        return widget.isFluidValid(drone.getFluidTank().getFluid().getFluid());
    }

    @Override
    public void startExecuting() {
        int amount = drone.getFluidTank().getFluidAmount();
        if (amount > 0 && widget.isFluidValid(drone.getFluidTank().getFluid().getFluid())) {
            drone.getFluidTank().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            drone.addAirToDrone(Math.max(1, PneumaticValues.DRONE_USAGE_VOID * amount / 20));
        }
    }
}
