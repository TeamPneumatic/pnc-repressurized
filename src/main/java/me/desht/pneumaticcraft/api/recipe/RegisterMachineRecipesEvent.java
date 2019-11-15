package me.desht.pneumaticcraft.api.recipe;

import net.minecraftforge.eventbus.api.Event;

import java.util.function.Consumer;

/**
 * This event is fired when PneumaticCraft machine recipes should be registered.  There are consumers for each
 * machine type; use <code>getType().accept(recipe)</code> to register a recipe.  Each recipe type has one or more
 * convenience methods to create a default recipe implementation, so you can say e.g.
 * <p>
 *     <code>event.getPressureChamber().accept(IPressureChamberRecipe.basicRecipe(...))</code>
 * </p>
 */
public class RegisterMachineRecipesEvent extends Event {
    private final Consumer<IPressureChamberRecipe> pressureChamber;
    private final Consumer<IThermopneumaticProcessingPlantRecipe> thermopneumatic;
    private final Consumer<IHeatFrameCoolingRecipe> heatFrameCooling;
    private final Consumer<IExplosionCraftingRecipe> explosionCrafting;
    private final Consumer<IRefineryRecipe> refinery;
    private final Consumer<IAssemblyRecipe> assembly;

    public RegisterMachineRecipesEvent(
            Consumer<IPressureChamberRecipe> pressureChamber,
            Consumer<IThermopneumaticProcessingPlantRecipe> thermopneumatic,
            Consumer<IHeatFrameCoolingRecipe> heatFrameCooling,
            Consumer<IExplosionCraftingRecipe> explosionCrafting,
            Consumer<IRefineryRecipe> refinery,
            Consumer<IAssemblyRecipe> assembly
            )
    {
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
