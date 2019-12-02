package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IRefineryRecipe;
import me.desht.pneumaticcraft.api.recipe.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.api.recipe.TemperatureRange;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid= Names.MOD_ID)
public class ModRefineryRecipes {
    @SubscribeEvent
    public static void register(RegisterMachineRecipesEvent evt) {
        addDefaultRefiningRecipe(evt, ModFluids.OIL);
    }

    public static void addDefaultRefiningRecipe(RegisterMachineRecipesEvent evt, Fluid fluid) {
        Consumer<IRefineryRecipe> ref = evt.getRefinery();

        String name = fluid.getRegistryName().getPath();
        String domain = fluid.getRegistryName().getNamespace();

        ref.accept(new RefineryRecipe(
                new ResourceLocation(domain, name + "_2"),
                new FluidStack(fluid, 10),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL, 4),
                new FluidStack(ModFluids.LPG, 2)
        ));

        ref.accept(new RefineryRecipe(
                new ResourceLocation(domain, name + "_3"),
                new FluidStack(fluid, 10),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL, 2),
                new FluidStack(ModFluids.KEROSENE, 3),
                new FluidStack(ModFluids.LPG, 2)
        ));

        ref.accept(new RefineryRecipe(
                new ResourceLocation(domain, name + "_4"),
                new FluidStack(fluid, 10),
                TemperatureRange.min(373),
                new FluidStack(ModFluids.DIESEL, 2),
                new FluidStack(ModFluids.KEROSENE, 3),
                new FluidStack(ModFluids.GASOLINE, 3),
                new FluidStack(ModFluids.LPG, 2)
        ));
    }
}
