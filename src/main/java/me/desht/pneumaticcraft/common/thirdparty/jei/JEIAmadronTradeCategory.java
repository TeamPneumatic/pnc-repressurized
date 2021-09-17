package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIAmadronTradeCategory extends AbstractPNCCategory<AmadronRecipe> {
    JEIAmadronTradeCategory() {
        super(ModCategoryUid.AMADRON_TRADE, AmadronRecipe.class,
                xlate(ModItems.AMADRON_TABLET.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.WIDGET_AMADRON_OFFER, 0, 0, 73, 35),
                guiHelper().createDrawableIngredient(new ItemStack(ModItems.AMADRON_TABLET.get()))
        );
    }

    @Override
    public void setIngredients(AmadronRecipe recipe, IIngredients ingredients) {
        recipe.getInput().accept(
                itemStack -> ingredients.setInput(VanillaTypes.ITEM, itemStack),
                fluidStack -> ingredients.setInput(VanillaTypes.FLUID, fluidStack)
        );
        recipe.getOutput().accept(
                itemStack -> ingredients.setOutput(VanillaTypes.ITEM, itemStack),
                fluidStack -> ingredients.setOutput(VanillaTypes.FLUID, fluidStack)
        );
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, AmadronRecipe recipe, IIngredients ingredients) {
        recipe.getInput().accept(
                itemStack -> {
                    recipeLayout.getItemStacks().init(0, true, 5, 14);
                    recipeLayout.getItemStacks().set(0, itemStack);
                },
                fluidStack -> {
                    recipeLayout.getFluidStacks().init(0, true, 6, 15, 16, 16,
                            1000, false, new FluidTextOverlay(fluidStack));
                    recipeLayout.getFluidStacks().set(0, fluidStack);
                }
        );

        recipe.getOutput().accept(
                itemStack -> {
                    recipeLayout.getItemStacks().init(1, false, 50, 14);
                    recipeLayout.getItemStacks().set(1, itemStack);
                },
                fluidStack -> {
                    recipeLayout.getFluidStacks().init(1, false, 51, 15, 16, 16,
                            1000, false, new FluidTextOverlay(fluidStack));
                    recipeLayout.getFluidStacks().set(1, fluidStack);
                }
        );

    }

    @Override
    public void draw(AmadronRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        FontRenderer fr = Minecraft.getInstance().font;
        int x = (getBackground().getWidth() - fr.width(recipe.getVendorName())) / 2;
        fr.draw(matrixStack, recipe.getVendorName(), x, 3, 0xFF404040);
    }

    @Override
    public List<ITextComponent> getTooltipStrings(AmadronRecipe recipe, double mouseX, double mouseY) {
        List<ITextComponent> res = new ArrayList<>();
        if (mouseX >= 22 && mouseX <= 51 && mouseY >= 12) {
            res.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.vendor", recipe.getVendorName()));
            res.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.selling", recipe.getOutput().toString()));
            res.add(xlate("pneumaticcraft.gui.amadron.amadronWidget.buying", recipe.getInput().toString()));
        }
        return res;
    }

    private static class FluidTextOverlay implements IDrawable {
        private final String text;

        FluidTextOverlay(FluidStack stack) {
            this.text = stack.getAmount() / 1000 + "B";
        }

        @Override
        public int getWidth() {
            return 16;
        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public void draw(MatrixStack matrixStack, int x, int y) {
            FontRenderer fr = Minecraft.getInstance().font;
            fr.drawShadow(matrixStack, text, x + getWidth() - fr.width(text), y + getHeight() - fr.lineHeight, 0xFFFFFFFF);
        }
    }
}
