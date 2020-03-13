package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.List;

public abstract class JEISpecialCraftingCategory<T extends IRecipeWrapper> extends PneumaticCraftCategory<T> {
    private List<String> text = Collections.emptyList();

    JEISpecialCraftingCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    protected void setText(String localizationKey, Object... parameters) {
        text = PneumaticCraftUtils.convertStringIntoList(I18n.format(localizationKey, parameters), 30);
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_NEI_MISC_RECIPES, 40, 0, 0, 0, 82, 18) {
            @Override
            public int getWidth() {
                return 160;
            }
        };
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        drawProgressBar(63, 0, 82, 0, 38, 18, StartDirection.LEFT);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return mouseX >= 63 && mouseX <= 101 ? text : Collections.emptyList();
    }

    protected abstract List<MultipleInputOutputRecipeWrapper> getAllRecipes();
}
