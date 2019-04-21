package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import me.desht.pneumaticcraft.common.recipes.PlasticMixerRegistry;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import net.minecraftforge.fluids.Fluid;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.liquidplastic")
@ZenRegister
public class LiquidPlastic {
	
	public static final String NAME = "PneumaticCraft Liquid Plastic Registry";
	
	@ZenMethod
	public static void addLiquidPlastic(ILiquidStack liquidInput, double ratio) {
		CraftTweaker.ADDITIONS.add(new LiquidPlasticAction(Helper.toFluid(liquidInput).getFluid(), (int)ratio));
	}

	@ZenMethod
    public static void removeLiquidPlastic(ILiquidStack liquidInput) {
		CraftTweaker.REMOVALS.add(new LiquidPlasticAction(Helper.toFluid(liquidInput).getFluid(), 0));
    }

	@ZenMethod
	public static void removeAllLiquidPlastics() {
		CraftTweaker.REMOVALS.add(new IAction(){
            @Override
            public void apply(){
                PlasticMixerRegistry.INSTANCE.clear();
            }

            @Override
            public String describe(){
                return "Removing all liquid plastic inputs.";
            }
		});
	}
	
    private static class LiquidPlasticAction implements IAction {
        private final Fluid fluid;
        private final int ratio;
        
        LiquidPlasticAction(Fluid fluid, int ratio) {
           this.fluid = fluid;
           this.ratio = ratio;
        }

        @Override
        public void apply(){
            PlasticMixerRegistry.INSTANCE.registerPlasticMixerInput(fluid, ratio);
        }
        
        @Override
        public String describe() {
            if (ratio == 0) {
                return String.format("Removing liquid plastic value for fluid %s", fluid.getName());
            } else {
                return String.format("Registering liquid plastic for fluid %s as %d mB per solid plastic.", fluid.getName(), ratio);
            }
        }
    }
}
