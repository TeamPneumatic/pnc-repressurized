package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;

public class JEIMemoryEssenceCategory implements IRecipeCategory<JEIMemoryEssenceCategory.MemoryEssenceRecipe> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    public JEIMemoryEssenceCategory() {
        localizedName = new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000).getDisplayName().getString();
        background = JEIPlugin.jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_MEMORY_ESSENCE, 0, 0, 146, 73);
        icon = JEIPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModItems.MEMORY_ESSENCE_BUCKET.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.MEMORY_ESSENCE;
    }

    @Override
    public Class<? extends MemoryEssenceRecipe> getRecipeClass() {
        return MemoryEssenceRecipe.class;
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
    public void setIngredients(MemoryEssenceRecipe recipe, IIngredients ingredients) {
        if (recipe.input2.isEmpty()) {
            ingredients.setInput(VanillaTypes.ITEM, recipe.input1);
        } else {
            ingredients.setInputs(VanillaTypes.ITEM, ImmutableList.of(recipe.input1, recipe.input2));
        }
        ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MemoryEssenceRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 53, 28);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        if (!recipe.input2.isEmpty()) {
            recipeLayout.getItemStacks().init(1, true, 75, 28);
            recipeLayout.getItemStacks().set(1, ingredients.getInputs(VanillaTypes.ITEM).get(1));
        }

        recipeLayout.getFluidStacks().init(0, false, 112, 29);
        recipeLayout.getFluidStacks().set(0, new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000));

        recipeLayout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            String tooltipKey = recipe.getTooltipKey(slotIndex);
            if (!tooltipKey.isEmpty()) {
                tooltip.addAll(PneumaticCraftUtils.splitStringComponent(TextFormatting.GREEN + I18n.get(tooltipKey)));
            }
        });
    }

    @Override
    public void draw(MemoryEssenceRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        FontRenderer fr = Minecraft.getInstance().font;
        int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
        String s = "1 XP = " + ratio + " mB";
        int w = fr.width(s);
        Minecraft.getInstance().font.draw(matrixStack, s, (background.getWidth() - w) / 2f, 0, 0x404040);
    }

    static Collection<MemoryEssenceRecipe> getAllRecipes() {
        return ImmutableList.of(
                new MemoryEssenceRecipe(ModItems.MEMORY_STICK.get(), null),
                new MemoryEssenceRecipe(ModBlocks.AERIAL_INTERFACE.get(), EnumUpgrade.DISPENSER.getItem()),
                new MemoryEssenceRecipe(ModItems.DRONE.get(), ModItems.PROGRAMMING_PUZZLE.get())
                        .setTooltipKey(1, "pneumaticcraft.gui.jei.tooltip.droneImportOrbs")
        );
    }

    static class MemoryEssenceRecipe {
        final ItemStack input1;
        final ItemStack input2;
        final String[] tooltips = new String[] {"", ""};

        public MemoryEssenceRecipe(IItemProvider input1, IItemProvider input2) {
            this.input1 = new ItemStack(input1);
            this.input2 = input2 == null ? ItemStack.EMPTY : new ItemStack(input2);
        }

        public MemoryEssenceRecipe setTooltipKey(int slot, String tooltipKey) {
            tooltips[slot] = tooltipKey;
            return this;
        }

        public String getTooltipKey(int slot) {
            return slot >= 0 && slot <= 2 ? tooltips[slot] : "";
        }
    }
}
