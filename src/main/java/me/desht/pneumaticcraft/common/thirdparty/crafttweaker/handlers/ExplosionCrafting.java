package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/ExplosionCrafting")
@ZenCodeType.Name("mods.pneumaticcraft.explosioncrafting")
@ZenRegister
public class ExplosionCrafting implements IRecipeManager {
    public static final String name = "PneumaticCraft Explosion Crafting";

    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, IItemStack[] outputs, int lossRate) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new ExplosionCraftingRecipeImpl(
                        new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toStackedIngredient(input),
                        lossRate,
                        CTUtils.toItemStacks(outputs))
                ));
    }

    @Override
    public IRecipeType<ExplosionCraftingRecipeImpl> getRecipeType() {
        return PneumaticCraftRecipeType.EXPLOSION_CRAFTING;
    }
}
