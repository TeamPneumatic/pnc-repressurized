package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.recipes.machine.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber(modid= Names.MOD_ID)
public class ModThermopneumaticRecipes {
    @SubscribeEvent
    public static void register(RegisterMachineRecipesEvent evt) {
        Consumer<IThermopneumaticProcessingPlantRecipe> tpp = evt.getThermopneumatic();

        tpp.accept(new BasicThermopneumaticProcessingPlantRecipe(
                RL("plastic"),
                new FluidStack(ModFluids.LPG, 100),
                Ingredient.fromTag(ItemTags.COALS),
                new FluidStack(ModFluids.PLASTIC, 1000),
                TemperatureRange.min(373), 0f,
                false
        ));

        tpp.accept(new BasicThermopneumaticProcessingPlantRecipe(
                RL("lubricant"),
                new FluidStack(ModFluids.DIESEL, 1000),
                Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE),
                new FluidStack(ModFluids.LUBRICANT, 1000),
                TemperatureRange.min(373), 0f,
                false
        ));

        tpp.accept(new BasicThermopneumaticProcessingPlantRecipe(
                RL("kerosene"),
                new FluidStack(ModFluids.DIESEL, 100),
                Ingredient.EMPTY,
                new FluidStack(ModFluids.KEROSENE, 80),
                TemperatureRange.min(573), 2.0f,
                false
        ));

        tpp.accept(new BasicThermopneumaticProcessingPlantRecipe(
                RL("gasoline"),
                new FluidStack(ModFluids.KEROSENE, 100),
                Ingredient.EMPTY,
                new FluidStack(ModFluids.GASOLINE, 80),
                TemperatureRange.min(573), 2.0f,
                false
        ));

        tpp.accept(new BasicThermopneumaticProcessingPlantRecipe(
                RL("lpg"),
                new FluidStack(ModFluids.GASOLINE, 100),
                Ingredient.EMPTY,
                new FluidStack(ModFluids.LPG, 80),
                TemperatureRange.min(573), 2.0f,
                false
        ));
    }

}
