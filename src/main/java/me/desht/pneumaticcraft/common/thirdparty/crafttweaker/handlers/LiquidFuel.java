package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import net.minecraftforge.fluids.Fluid;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;

@ZenClass("mods.pneumaticcraft.liquidfuel")
@ZenRegister
public class LiquidFuel {
	
	public static final String NAME = "PneumaticCraft Liquid Fuel";
	
	@ZenMethod
	public static void addFuel(ILiquidStack liquidInput, double mLPerBucket) {
		CraftTweaker.ADDITIONS.add(new LiquidFuelAction(Helper.toFluid(liquidInput).getFluid(), (int)mLPerBucket));
	}

	@ZenMethod
    public static void removeFuel(ILiquidStack liquidInput) {
		CraftTweaker.REMOVALS.add(new LiquidFuelAction(Helper.toFluid(liquidInput).getFluid(), 0));
    }

	@ZenMethod
	public static void removeAllFuels() {
		CraftTweaker.REMOVALS.add(new IAction(){
            @Override
            public void apply(){
                PneumaticCraftAPIHandler.getInstance().liquidFuels.clear();
            }

            @Override
            public String describe(){
                return "Removing all fuel values.";
            }
		});
	}
	
    private static class LiquidFuelAction implements IAction {
        private final Fluid fluid;
        private final int mlPerBucket;
        
        public LiquidFuelAction(Fluid fluid, int mlPerBucket) {
           this.fluid = fluid;
           this.mlPerBucket = mlPerBucket;
        }

        @Override
        public void apply(){
            PneumaticCraftAPIHandler.getInstance().registerFuel(fluid, mlPerBucket);
        }
        
        @Override
        public String describe() {
            if(mlPerBucket == 0){
                return String.format("Removing fuel value for fluid %s", fluid.getName());
            }else{
                return String.format("Registering fuel value %d for fluid %s.", mlPerBucket, fluid.getName());
            }
        }
    }
}
