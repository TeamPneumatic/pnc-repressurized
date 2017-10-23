package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class JEICompressedIronCategory extends JEISpecialCraftingCategory<JEICompressedIronCategory.CompressedIronExplosionWrapper> {
    private static IDrawable icon;

    JEICompressedIronCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
        setText("gui.nei.recipe.compressedIronExplosion", ConfigHandler.general.configCompressedIngotLossRate);

        icon = jeiHelpers.getGuiHelper().createDrawable(Textures.JEI_EXPLOSION, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected List<MultipleInputOutputRecipeWrapper> getAllRecipes() {
        List<MultipleInputOutputRecipeWrapper> recipes = new ArrayList<>();
        MultipleInputOutputRecipeWrapper recipe = new CompressedIronExplosionWrapper();
        recipe.addIngredient(new PositionedStack(new ItemStack(Items.IRON_INGOT), 41, 80));
        recipe.addOutput(new PositionedStack(new ItemStack(Itemss.INGOT_IRON_COMPRESSED), 105, 80));
        recipes.add(recipe);
        return recipes;
    }

    @Override
    public String getUid() {
        return ModCategoryUid.COMPRESSED_IRON_EXPLOSION;
    }

    @Override
    public String getTitle() {
        return I18n.format("gui.nei.title.compressedIronExplosion");
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    static class CompressedIronExplosionWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableDepth();
            GlStateManager.enableAlpha();
            icon.draw(Minecraft.getMinecraft(), 73, 80);
            GlStateManager.enableDepth();
            GlStateManager.disableAlpha();
        }
    }
}
