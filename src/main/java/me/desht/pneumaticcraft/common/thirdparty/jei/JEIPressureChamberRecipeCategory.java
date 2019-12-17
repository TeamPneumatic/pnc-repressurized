package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.recipe.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JEIPressureChamberRecipeCategory extends PneumaticCraftCategory<JEIPressureChamberRecipeCategory.PressureChamberRecipeWrapper> {
    private final String localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    JEIPressureChamberRecipeCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);

        background = jeiHelpers.getGuiHelper().createDrawable(Textures.GUI_JEI_PRESSURE_CHAMBER_LOCATION, 5, 11, 166, 130);
        icon = jeiHelpers.getGuiHelper().createDrawableIngredient(new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL));
        localizedName = I18n.format("gui.pressureChamber");
    }

    @Override
    public ResourceLocation getUid() {
        return ModCategoryUid.PRESSURE_CHAMBER;
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
    public Class<? extends PressureChamberRecipeWrapper> getRecipeClass() {
        return PressureChamberRecipeWrapper.class;
    }

    static Collection<PressureChamberRecipeWrapper> getAllRecipes() {
        return PneumaticCraftRecipes.pressureChamberRecipes.values().stream()
            .map(PressureChamberRecipeWrapper::new)
            .collect(Collectors.toList());
    }

    static class PressureChamberRecipeWrapper extends PneumaticCraftCategory.AbstractCategoryExtension {
        PressureChamberRecipeWrapper(IPressureChamberRecipe recipe) {
            List<List<ItemStack>> inputs = recipe.getInputsForDisplay();
            for (int i = 0; i < inputs.size(); i++) {
                int posX = 19 + i % 3 * 17;
                int posY = 93 - i / 3 * 17;
                PositionedStack pStack = PositionedStack.of(inputs.get(i), posX, posY);
                this.addInputItem(pStack);
            }

            for (int i = 0; i < recipe.getResultForDisplay().size(); i++) {
                ItemStack stack = recipe.getResultForDisplay().get(i);
                PositionedStack pStack = PositionedStack.of(stack, 101 + i % 3 * 18, 59 + i / 3 * 18)
                        .setTooltipKey(IPressureChamberRecipe.getTooltipKey(stack));
                this.addOutputItem(pStack);
            }

            setUsedPressure(120, 27, recipe.getCraftingPressure(), PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.DANGER_PRESSURE_TIER_ONE);
        }
    }
}
