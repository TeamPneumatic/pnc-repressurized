package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.FluidMixerRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/FluidMixer")
@ZenCodeType.Name("mods.pneumaticcraft.FluidMixer")
@ZenRegister
public class FluidMixer implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient input1, CTFluidIngredient input2, IFluidStack outputFluid, IItemStack outputItem, float pressure, int processingTime) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new FluidMixerRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toFluidIngredient(input1),
                        CTUtils.toFluidIngredient(input2),
                        outputFluid.getImmutableInternal(),
                        outputItem.getImmutableInternal(),
                        pressure,
                        processingTime)
        ));
    }

    @Override
    public IRecipeType<FluidMixerRecipe> getRecipeType() {
        return PneumaticCraftRecipeType.FLUID_MIXER;
    }
}
