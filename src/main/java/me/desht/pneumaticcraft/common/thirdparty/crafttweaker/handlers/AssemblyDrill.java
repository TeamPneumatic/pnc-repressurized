package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/AssemblyDrill")
@ZenRegister
@ZenCodeType.Name("mods.pneumaticcraft.AssemblyDrill")
public class AssemblyDrill implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, IItemStack output) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new AssemblyRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toStackedIngredient(input),
                        output.getImmutableInternal(),
                        AssemblyRecipe.AssemblyProgramType.DRILL))
        );
    }

    @Override
    public IRecipeType<AssemblyRecipe> getRecipeType() {
        return PneumaticCraftRecipeType.ASSEMBLY_DRILL;
    }
}
