package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import net.minecraftforge.fluids.Fluid;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.xpfluid")
@ZenRegister
public class XPFluid {
    @ZenMethod
    public static void addXPFluid(ILiquidStack liquidInput, double xpRatio) {
        CraftTweaker.ADDITIONS.add(new FluidXPAction(Helper.toFluid(liquidInput).getFluid(), (int)xpRatio));
    }

    @ZenMethod
    public static void removeXPFluid(ILiquidStack liquidInput) {
        CraftTweaker.REMOVALS.add(new FluidXPAction(Helper.toFluid(liquidInput).getFluid(), 0));
    }

    @ZenMethod
    public static void removeAllXPFluids() {
        CraftTweaker.REMOVALS.add(new IAction(){
            @Override
            public void apply(){
                PneumaticCraftAPIHandler.getInstance().liquidXPs.clear();
                PneumaticCraftAPIHandler.getInstance().availableLiquidXPs.clear();
            }

            @Override
            public String describe(){
                return "Removing all XP fluid values.";
            }
        });
    }

    private static class FluidXPAction implements IAction {
        private final Fluid fluid;
        private final int xpRatio; // mB per XP point

        public FluidXPAction(Fluid fluid, int xpRatio) {
            this.fluid = fluid;
            this.xpRatio = xpRatio;
        }

        @Override
        public void apply(){
            PneumaticCraftAPIHandler.getInstance().registerXPLiquid(fluid, xpRatio);
        }

        @Override
        public String describe() {
            if(xpRatio == 0){
                return String.format("Removing XP value for fluid %s", fluid.getName());
            }else{
                return String.format("Registering XP value %d for fluid %s.", xpRatio, fluid.getName());
            }
        }
    }
}
