package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IAssemblyRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JEIAssemblyControllerCategory extends PneumaticCraftCategory<JEIAssemblyControllerCategory.AssemblyRecipeWrapper> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progressBar;

    JEIAssemblyControllerCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        background = jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_NEI_ASSEMBLY_CONTROLLER, 5, 11, 166, 130);
        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER));
        localizedName = I18n.format(ModBlocks.ASSEMBLY_CONTROLLER.getTranslationKey());
        IDrawableStatic d = jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_NEI_ASSEMBLY_CONTROLLER, 173, 0, 24, 17);
        progressBar = jeiHelpers.getGuiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.ASSEMBLY_CONTROLLER;
    }

    @Override
    public Class<? extends JEIAssemblyControllerCategory.AssemblyRecipeWrapper> getRecipeClass() {
        return AssemblyRecipeWrapper.class;
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
    public void draw(AssemblyRecipeWrapper recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);

        progressBar.draw(68, 75);
//        drawProgressBar(Textures.GUI_NEI_ASSEMBLY_CONTROLLER, 68, 75, 173, 0, 24, 17, IDrawableAnimated.StartDirection.LEFT);

        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        fontRenderer.drawString("Required Machines", 5, 15, 0xFF404040);
        fontRenderer.drawString("Prog.", 129, 9, 0xFF404040);
    }

    static Collection<AssemblyRecipeWrapper> getAllRecipes() {
        List<AssemblyRecipeWrapper> res = new ArrayList<>();
        for (IAssemblyRecipe recipe : PneumaticCraftRecipes.assemblyDrillRecipes.values()) {
            res.add(new AssemblyRecipeWrapper(recipe));
        }
        for (IAssemblyRecipe recipe : PneumaticCraftRecipes.assemblyLaserRecipes.values()) {
            res.add(new AssemblyRecipeWrapper(recipe));
        }
        for (IAssemblyRecipe recipe : PneumaticCraftRecipes.assemblyLaserDrillRecipes.values()) {
            res.add(new AssemblyRecipeWrapper(recipe));
        }
        return res;
    }

    static class AssemblyRecipeWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        AssemblyRecipeWrapper(IAssemblyRecipe recipe) {
            AssemblyProgram program = AssemblyProgram.fromRecipe(recipe);

            //for now not useful to put it in an array, but supports when adding multiple input/output.
            ItemStack[] inputStacks = new ItemStack[]{recipe.getInput().getMatchingStacks()[0]};
            for (int i = 0; i < inputStacks.length; i++) {
                PositionedStack stack = PositionedStack.of(inputStacks[i], 29 + i % 2 * 18, 66 + i / 2 * 18);
                this.addInputItem(stack);
            }

            ItemStack[] outputStacks = new ItemStack[]{recipe.getOutput()};
            for (int i = 0; i < outputStacks.length; i++) {
                PositionedStack stack = PositionedStack.of(outputStacks[i], 96 + i % 2 * 18, 66 + i / 2 * 18);
                this.addOutputItem(stack);
            }
            this.addInputItem(PositionedStack.of(program.getItemStack(1), 133, 22));
            ItemStack[] requiredMachines = getMachinesFromEnum(program.getRequiredMachines());
            for (int i = 0; i < requiredMachines.length; i++) {
                this.addInputItem(PositionedStack.of(requiredMachines[i], 5 + i * 18, 25));
            }
        }

        private ItemStack[] getMachinesFromEnum(AssemblyProgram.EnumMachine[] requiredMachines) {
            ItemStack[] machineStacks = new ItemStack[requiredMachines.length];
            for (int i = 0; i < requiredMachines.length; i++) {
                machineStacks[i] = new ItemStack(requiredMachines[i].getMachine());
            }
            return machineStacks;
        }
    }
}
