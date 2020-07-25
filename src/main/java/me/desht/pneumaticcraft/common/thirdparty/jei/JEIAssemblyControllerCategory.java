package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JEIAssemblyControllerCategory implements IRecipeCategory<AssemblyRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progressBar;

    JEIAssemblyControllerCategory() {
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_ASSEMBLY_CONTROLLER, 5, 11, 166, 130);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER.get()));
        localizedName = I18n.format(ModBlocks.ASSEMBLY_CONTROLLER.get().getTranslationKey());
        IDrawableStatic d = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_ASSEMBLY_CONTROLLER, 173, 0, 24, 17);
        progressBar = JEIPlugin.jeiHelpers.getGuiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.ASSEMBLY_CONTROLLER;
    }

    @Override
    public Class<? extends AssemblyRecipe> getRecipeClass() {
        return AssemblyRecipe.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(AssemblyRecipe recipe, IIngredients ingredients) {
        List<Ingredient> input = new ArrayList<>();
        input.add(recipe.getInput());
        input.add(Ingredient.fromItems(ItemAssemblyProgram.fromProgramType(recipe.getProgramType())));
        Arrays.stream(getMachinesFromEnum(AssemblyProgram.fromRecipe(recipe).getRequiredMachines()))
                .map(Ingredient::fromStacks)
                .forEach(input::add);
        ingredients.setInputIngredients(input);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, AssemblyRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 28, 65);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(2, true, 132, 21);
        recipeLayout.getItemStacks().set(2, ingredients.getInputs(VanillaTypes.ITEM).get(1));

        int l = ingredients.getInputs(VanillaTypes.ITEM).size() - 2;  // -2 for the input item & program
        for (int i = 0; i < l; i++) {
            recipeLayout.getItemStacks().init(i + 3, true, 5 + i * 18, 25);
            recipeLayout.getItemStacks().set(i + 3, ingredients.getInputs(VanillaTypes.ITEM).get(i + 2));
        }

        recipeLayout.getItemStacks().init(1, false, 95, 65);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(AssemblyRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        progressBar.draw(matrixStack, 68, 75);
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        fontRenderer.drawString(matrixStack, "Required Machines", 5, 15, 0xFF404040);
        fontRenderer.drawString(matrixStack, "Prog.", 129, 9, 0xFF404040);
    }

    private ItemStack[] getMachinesFromEnum(AssemblyProgram.EnumMachine[] requiredMachines) {
        ItemStack[] machineStacks = new ItemStack[requiredMachines.length];
        for (int i = 0; i < requiredMachines.length; i++) {
            machineStacks[i] = new ItemStack(requiredMachines[i].getMachineBlock());
        }
        return machineStacks;
    }
}
