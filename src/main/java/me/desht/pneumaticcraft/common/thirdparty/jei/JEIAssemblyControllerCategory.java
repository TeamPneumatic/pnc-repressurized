package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IAssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram.EnumMachine.IO_UNIT_EXPORT;
import static me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram.EnumMachine.IO_UNIT_IMPORT;

public class JEIAssemblyControllerCategory extends JEIPneumaticCraftCategory<IAssemblyRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIAssemblyControllerCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        background = jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_NEI_ASSEMBLY_CONTROLLER, 5, 11, 166, 130);
        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER));
        localizedName = I18n.format(ModBlocks.ASSEMBLY_CONTROLLER.getTranslationKey());
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.ASSEMBLY_CONTROLLER;
    }

    @Override
    public Class<? extends IAssemblyRecipe> getRecipeClass() {
        return IAssemblyRecipe.class;
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
    public void setIngredients(IAssemblyRecipe recipe, IIngredients ingredients) {
        AssemblyProgram program = AssemblyProgram.fromRecipe(recipe);

        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(Arrays.asList(recipe.getInput().getMatchingStacks()));
        inputs.add(Collections.singletonList(new ItemStack(recipe.getProgram())));
        for (ItemStack stack: getMachines(program.getRequiredMachines())) {
            inputs.add(Collections.singletonList(stack));
        }
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);

        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IAssemblyRecipe recipe, IIngredients ingredients) {
        // TODO one day support multiple inputs/outputs
        recipeLayout.getItemStacks().init(0, true, 29, 66);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(1, true, 133, 22);
        recipeLayout.getItemStacks().set(1, ingredients.getInputs(VanillaTypes.ITEM).get(1));

        int nMachines = ingredients.getInputs(VanillaTypes.ITEM).size();
        for (int i = 0; i < nMachines; i++) {
            recipeLayout.getItemStacks().init(i + 2, true, 5 + i * 18, 25);
            recipeLayout.getItemStacks().set(i + 2, ingredients.getInputs(VanillaTypes.ITEM).get(i + 2));
        }

        recipeLayout.getItemStacks().init(nMachines + 2, false, 96, 66);
        recipeLayout.getItemStacks().set(nMachines + 2, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    private List<ItemStack> getMachines(AssemblyProgram.EnumMachine[] requiredMachines) {
        List<ItemStack> res = new ArrayList<>();

        for (AssemblyProgram.EnumMachine m : requiredMachines) {
            switch (m) {
                case PLATFORM:
                    res.add(new ItemStack(ModBlocks.ASSEMBLY_PLATFORM));
                    break;
                case DRILL:
                    res.add(new ItemStack(ModBlocks.ASSEMBLY_DRILL));
                    break;
                case LASER:
                    res.add(new ItemStack(ModBlocks.ASSEMBLY_LASER));
                    break;
                case IO_UNIT_IMPORT:
                    res.add(makeIOUnitStack(IO_UNIT_IMPORT));
                    break;
                case IO_UNIT_EXPORT:
                    res.add(makeIOUnitStack(IO_UNIT_EXPORT));
                    break;
            }
        }
        return res;
    }

    @Nonnull
    private ItemStack makeIOUnitStack(AssemblyProgram.EnumMachine what) {
        ItemStack stack = new ItemStack(ModBlocks.ASSEMBLY_IO_UNIT);
        stack.getOrCreateTag().putBoolean("Import", what == IO_UNIT_IMPORT);
        return stack;
    }

    @Override
    public void draw(IAssemblyRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);

        drawProgressBar(Textures.GUI_NEI_ASSEMBLY_CONTROLLER, 68, 75, 173, 0, 24, 17, IDrawableAnimated.StartDirection.LEFT);

        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        fontRenderer.drawString("Required Machines", 5, 15, 0xFF404040);
        fontRenderer.drawString("Prog.", 129, 9, 0xFF404040);
    }

}
