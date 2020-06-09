package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariableProvider;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.util.ItemStackUtil;

public class ProcessorAssemblySystem implements IComponentProcessor {
    private AssemblyRecipe recipe = null;

    @Override
    public void setup(IVariableProvider<String> iVariableProvider) {
        ItemStack result = PatchouliAPI.instance.deserializeItemStack(iVariableProvider.get("item"));
        recipe = findRecipe(result);
    }

    @Override
    public String process(String key) {
        if (recipe == null) return null;

        ItemStack programStack = new ItemStack(ItemAssemblyProgram.fromProgramType(recipe.getProgramType()));
        switch (key) {
            case "input":
                return ItemStackUtil.serializeIngredient(recipe.getInput());
            case "output":
                return ItemStackUtil.serializeStack(recipe.getOutput());
            case "program":
                return ItemStackUtil.serializeStack(programStack);
            case "name":
                return recipe.getOutput().getDisplayName().getFormattedText();
            case "desc":
                return I18n.format("pneumaticcraft.patchouli.processor.assembly.desc",
                        recipe.getOutput().getDisplayName().getFormattedText(),
                        programStack.getDisplayName().getFormattedText()
                );
        }

        return null;
    }

    private AssemblyRecipe findRecipe(ItemStack result) {
        World world = Minecraft.getInstance().world;

        for (AssemblyRecipe recipe : PneumaticCraftRecipeType.ASSEMBLY_DRILL.getRecipes(world).values()) {
            if (ItemStack.areItemsEqual(recipe.getOutput(), result)) return recipe;
        }
        for (AssemblyRecipe recipe : PneumaticCraftRecipeType.ASSEMBLY_LASER.getRecipes(world).values()) {
            if (ItemStack.areItemsEqual(recipe.getOutput(), result)) return recipe;
        }
        for (AssemblyRecipe recipe : PneumaticCraftRecipeType.ASSEMBLY_DRILL_LASER.getRecipes(world).values()) {
            if (ItemStack.areItemsEqual(recipe.getOutput(), result)) return recipe;
        }
        return null;
    }
}
