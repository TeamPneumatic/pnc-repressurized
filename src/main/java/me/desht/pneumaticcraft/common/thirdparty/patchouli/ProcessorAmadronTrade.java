package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource.Type;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

@SuppressWarnings("unused")
public class ProcessorAmadronTrade implements IComponentProcessor {
    private AmadronRecipe recipe = null;
    private String text = null;

    @Override
    public void setup(IVariableProvider iVariableProvider) {
        ResourceLocation recipeId = new ResourceLocation(iVariableProvider.get("recipe").asString());
        recipe = PneumaticCraftRecipeType.AMADRON_OFFERS.getRecipe(Minecraft.getInstance().world, recipeId);
        if (recipe == null) {
            Log.warning("Missing amadron offer recipe: " + recipeId);
        }

        text = iVariableProvider.has("text") ? iVariableProvider.get("text").asString() : null;
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null) return null;

        switch (key) {
            case "input":
                return IVariable.from(recipe.getInput().getType() == Type.ITEM ? recipe.getInput().getItem() : recipe.getInput().getFluid());
            case "output":
                return IVariable.from(recipe.getOutput().getType() == Type.ITEM ? recipe.getOutput().getItem() : recipe.getOutput().getFluid());
            case "name":
                return IVariable.wrap(recipe.getOutput().getItem().getDisplayName().getString());
            case "text":
                return IVariable.wrap(text == null ? "" : I18n.format(text));
        }

        return null;
    }
}
