package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.PlasticMixerRegistry;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.liquidplastic")
@ZenRegister
public class LiquidPlastic {
	
	private static final String NAME = "PneumaticCraft Plastic Mixer";

    @ZenMethod
    public static void addRecipe(ILiquidStack liquidInput, IItemStack stack, int temperature) {
        CraftTweaker.ADDITIONS.add(new LiquidPlasticAction(Helper.toFluid(liquidInput), Helper.toStack(stack), temperature, true, true));
    }

    @ZenMethod
    public static void addMeltOnlyRecipe(ILiquidStack liquidInput, IItemStack stack, int temperature) {
        CraftTweaker.ADDITIONS.add(new LiquidPlasticAction(Helper.toFluid(liquidInput), Helper.toStack(stack), temperature, true, false));
    }

    @ZenMethod
    public static void addSolidifyOnlyRecipe(ILiquidStack liquidInput, IItemStack stack) {
        CraftTweaker.ADDITIONS.add(new LiquidPlasticAction(Helper.toFluid(liquidInput), Helper.toStack(stack), 0, false, true));
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack liquidInput) {
        CraftTweaker.REMOVALS.add(new LiquidPlasticAction(Helper.toFluid(liquidInput).getFluid(), 0));
    }

	@ZenMethod
	public static void removeAllRecipes() {
		CraftTweaker.REMOVALS.add(new IAction(){
            @Override
            public void apply(){
                PlasticMixerRegistry.INSTANCE.clear();
            }

            @Override
            public String describe(){
                return "Removing all " + NAME + " recipes.";
            }
		});
	}
	
    private static class LiquidPlasticAction implements IAction {
        private final FluidStack fluidStack;
        private final ItemStack stack;
        private final int temperature;
        private final boolean allowMelting;
        private final boolean allowSolidifying;

        LiquidPlasticAction(FluidStack fluidStack, ItemStack stack, int temperature, boolean allowMelting, boolean allowSolidifying) {
            this.fluidStack = fluidStack;
            this.stack = stack;
            this.temperature = temperature;
            this.allowMelting = allowMelting;
            this.allowSolidifying = allowSolidifying;
        }

        LiquidPlasticAction(FluidStack fluidStack, ItemStack stack) {
            this.fluidStack = fluidStack;
            this.stack = stack;
            this.allowMelting = true;
            this.allowSolidifying = true;
            this.temperature = PneumaticValues.PLASTIC_MIXER_MELTING_TEMP;
        }

        LiquidPlasticAction(Fluid fluid, int ratio) {
            this(new FluidStack(fluid, ratio), new ItemStack(Itemss.PLASTIC));
        }

        @Override
        public void apply(){
            PneumaticRecipeRegistry.getInstance().registerPlasticMixerRecipe(fluidStack, stack, temperature, allowMelting, allowSolidifying);
        }
        
        @Override
        public String describe() {
            if (fluidStack.amount == 0) {
                return String.format("Removing liquid plastic value for fluid %s", fluidStack.getFluid().getName());
            } else {
                return String.format("Registering liquid plastic for fluid %s as %d mB per solid plastic.", fluidStack.getFluid().getName(), fluidStack.amount);
            }
        }
    }
}
