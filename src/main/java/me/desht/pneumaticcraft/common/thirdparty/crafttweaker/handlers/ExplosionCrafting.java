package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.oredict.IOreDictEntry;
import me.desht.pneumaticcraft.common.recipes.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CraftTweaker;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.Helper;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListAddition;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.ListRemoval;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.util.RemoveAllRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.pneumaticcraft.explosioncrafting")
@ZenRegister
public class ExplosionCrafting {
    public static final String name = "PneumaticCraft Explosion Crafting";

    @ZenMethod
    public static void addRecipe(IOreDictEntry input, IItemStack output, int lossRate) {
        CraftTweaker.ADDITIONS.add(new Add(new ExplosionCraftingRecipe(input.getName(), Helper.toStack(output), lossRate)));
    }

    @ZenMethod
    public static void addRecipe(IItemStack input, IItemStack output, int lossRate) {
        CraftTweaker.ADDITIONS.add(new Add(new ExplosionCraftingRecipe(Helper.toStack(input), Helper.toStack(output), lossRate)));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient output) {
        CraftTweaker.REMOVALS.add(new Remove(output));
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CraftTweaker.REMOVALS.add(new RemoveAllRecipes<>(ExplosionCrafting.name, ExplosionCraftingRecipe.recipes));
    }

    private static class Add extends ListAddition<ExplosionCraftingRecipe> {
        public Add(ExplosionCraftingRecipe recipe) {
            super(ExplosionCrafting.name, ExplosionCraftingRecipe.recipes, recipe);
        }
    }

    private static class Remove extends ListRemoval<ExplosionCraftingRecipe> {
        private final IIngredient output;

        public Remove(IIngredient output) {
            super(ExplosionCrafting.name, ExplosionCraftingRecipe.recipes);
            this.output = output;
        }

        @Override
        public void apply() {
            addRecipes();
            super.apply();
        }

        private void addRecipes() {
            for (ExplosionCraftingRecipe r : recipes) {
                if (Helper.matches(output,  Helper.toIItemStack(r.getOutput()))) {
                    entries.add(r);
                }
            }

            if (entries.isEmpty()) {
                Helper.logWarning(String.format("No %s Recipe found for %s. Command ignored!", name, Helper.getStackDescription(output)));
            } else {
                Helper.logInfo(String.format("Found %d %s Recipe(s) for %s.", entries.size(), name, Helper.getStackDescription(output)));
            }
        }

        @Override
        public String describe() {
            return String.format("Removing %s Recipe(s) for %s", this.name, Helper.getStackDescription(output));
        }
    }
}
