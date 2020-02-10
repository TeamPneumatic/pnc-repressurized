package me.desht.pneumaticcraft.api.crafting;

import me.desht.pneumaticcraft.api.crafting.recipe.*;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Consumer;

public class RegisterMachineRecipesEvent extends Event {
    /**
     * This event is fired when PneumaticCraft machine recipes should be registered, which is when the server starts up
     * or is reloaded with the /reload command.  There are consumers for each machine type; use
     * <code>getType().accept(recipe)</code> to register a recipe. {@link IPneumaticRecipeRegistry} has convenience methods
     * to create recipe implementations for each machine type, so you can say e.g.
     *     <pre>{@code
     *     IPneumaticRecipeRegistry recipeRegistry = PneumaticRegistry.getRecipeRegistry();
     *     event.getPressureChamber().accept(recipeRegistry.pressureChamberRecipe(...));
     *     }</pre>
     * Consider whether you really need to use this event; adding or removing recipes via datapacks is preferable.
     */
    public static class Pre extends RegisterMachineRecipesEvent {
        private final Consumer<IPressureChamberRecipe> pressureChamber;
        private final Consumer<IThermopneumaticProcessingPlantRecipe> thermopneumatic;
        private final Consumer<IHeatFrameCoolingRecipe> heatFrameCooling;
        private final Consumer<IExplosionCraftingRecipe> explosionCrafting;
        private final Consumer<IRefineryRecipe> refinery;
        private final Consumer<IAssemblyRecipe> assembly;

        public Pre(
                Consumer<IPressureChamberRecipe> pressureChamber,
                Consumer<IThermopneumaticProcessingPlantRecipe> thermopneumatic,
                Consumer<IHeatFrameCoolingRecipe> heatFrameCooling,
                Consumer<IExplosionCraftingRecipe> explosionCrafting,
                Consumer<IRefineryRecipe> refinery,
                Consumer<IAssemblyRecipe> assembly
        ) {
            this.pressureChamber = pressureChamber;
            this.thermopneumatic = thermopneumatic;
            this.heatFrameCooling = heatFrameCooling;
            this.explosionCrafting = explosionCrafting;
            this.refinery = refinery;
            this.assembly = assembly;
        }

        public Consumer<IPressureChamberRecipe> getPressureChamber() {
            return pressureChamber;
        }

        public Consumer<IThermopneumaticProcessingPlantRecipe> getThermopneumatic() {
            return thermopneumatic;
        }

        public Consumer<IHeatFrameCoolingRecipe> getHeatFrameCooling() {
            return heatFrameCooling;
        }

        public Consumer<IExplosionCraftingRecipe> getExplosionCrafting() {
            return explosionCrafting;
        }

        public Consumer<IRefineryRecipe> getRefinery() {
            return refinery;
        }

        public Consumer<IAssemblyRecipe> getAssembly() {
            return assembly;
        }
    }

    /**
     * This event is fired after all recipes have been reloaded.  It could be used to cache any important (and
     * expensive to compute) recipe information, for example.
     */
    public static class Post extends RegisterMachineRecipesEvent {
    }
}
