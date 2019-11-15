package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class JEIPressureChamberRecipeCategory extends JEIPneumaticCraftCategory<IPressureChamberRecipe> {
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
    public void setIngredients(IPressureChamberRecipe recipe, IIngredients iIngredients) {
        iIngredients.setInputLists(VanillaTypes.ITEM, recipe.getInputsForDisplay());
        iIngredients.setOutputs(VanillaTypes.ITEM, recipe.getResultForDisplay());

        setUsedPressure(120, 27, recipe.getCraftingPressure(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IPressureChamberRecipe recipe, IIngredients iIngredients) {
        List<List<ItemStack>> inputs = iIngredients.getInputs(VanillaTypes.ITEM);
        for (int i = 0; i < inputs.size(); i++) {
            layout.getItemStacks().init(i, true, 19 + i % 3 * 17, 93 - i / 3 * 17);
            layout.getItemStacks().set(i, inputs.get(i));
        }

        List<List<ItemStack>> outputs = iIngredients.getOutputs(VanillaTypes.ITEM);
        for (int i = 0; i < outputs.size(); i++) {
            layout.getItemStacks().init(inputs.size() + i, false, 101 + i % 3 * 18, 59 + i / 3 * 18);
            layout.getItemStacks().set(inputs.size() + i, outputs.get(i));
        }
    }

    @Override
    public Class<? extends IPressureChamberRecipe> getRecipeClass() {
        return IPressureChamberRecipe.class;
    }

}
