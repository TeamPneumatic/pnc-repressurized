package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionRemoveRecipe;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.PressureChamberRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Collection;

@Document("mods/PneumaticCraft/PressureChamber")
@ZenCodeType.Name("mods.pneumaticcraft.pressurechamber")
@ZenRegister
public class PressureChamber implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount[] inputs, IItemStack[] outputs, float pressure) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new PressureChamberRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toStackedIngredientList(inputs),
                        pressure,
                        CTUtils.toItemStacks(outputs))
        ));
    }

    @Override
    public void removeRecipe(IIngredient output) {
        CraftTweakerAPI.apply(new ActionRemoveRecipe(this, iRecipe -> {
            if (iRecipe instanceof PressureChamberRecipe) {
                return ((PressureChamberRecipe) iRecipe).getResultsForDisplay().stream()
                        .flatMap(Collection::stream)
                        .anyMatch(stack -> output.matches(new MCItemStack(stack)));
            }
            return false;
        }));
    }

    @Override
    public IRecipeType<PressureChamberRecipe> getRecipeType() {
        return PneumaticCraftRecipeType.PRESSURE_CHAMBER;
    }
}
