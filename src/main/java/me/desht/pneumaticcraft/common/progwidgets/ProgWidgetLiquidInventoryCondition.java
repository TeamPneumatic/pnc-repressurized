package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class ProgWidgetLiquidInventoryCondition extends ProgWidgetCondition {

    @Override
    public String getWidgetString() {
        return "conditionLiquidInventory";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetLiquidFilter.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                TileEntity te = drone.world().getTileEntity(pos);
                int count = te == null ? countFluid(drone.world(), pos) : countFluid(te);
                return ((ICondition) progWidget).getOperator().evaluate(count, ((ICondition) progWidget).getRequiredCount());
            }

            private int countFluid(World world, BlockPos pos) {
                FluidStack fluidStack = FluidUtils.getFluidAt(world, pos, false);
                if (fluidStack != null && ProgWidgetLiquidFilter.isLiquidValid(fluidStack.getFluid(), progWidget, 1)) {
                    return 1000;
                } else {
                    return 0;
                }
            }

            private int countFluid(TileEntity te) {
                return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(handler -> {
                    int total = 0;
                    for (IFluidTankProperties prop : handler.getTankProperties()) {
                        FluidStack stack = prop.getContents();
                        if (stack != null) {
                            if (ProgWidgetLiquidFilter.isLiquidValid(stack.getFluid(), progWidget, 1)) {
                                total += stack.amount;
                            }
                        }
                    }
                    return total;
                }).orElse(0);
            }
        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_LIQUID_INVENTORY;
    }

}
