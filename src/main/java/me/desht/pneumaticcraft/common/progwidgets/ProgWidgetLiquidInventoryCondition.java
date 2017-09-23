package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
                int count = 0;
                if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) ) {
                    IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                    for (IFluidTankProperties prop : handler.getTankProperties()) {
                        FluidStack stack = prop.getContents();
                        if (stack != null) {
                            if (ProgWidgetLiquidFilter.isLiquidValid(stack.getFluid(), widget, 1)) {
                                count += stack.amount;
                            }
                        }
                    }
                } else {
                    Fluid fluid = FluidRegistry.lookupFluidForBlock(drone.world().getBlockState(pos).getBlock());
                    if (fluid != null && ProgWidgetLiquidFilter.isLiquidValid(fluid, widget, 1) && FluidUtils.isSourceBlock(drone.world(), pos)) {
                        count += 1000;
                    }
                }
                return ((ICondition) widget).getOperator() == ICondition.Operator.EQUALS ?
                        count == ((ICondition) widget).getRequiredCount() :
                        count >= ((ICondition) widget).getRequiredCount();
            }

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_LIQUID_INVENTORY;
    }

}
