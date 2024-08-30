package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIElectroStaticGridCategory extends AbstractPNCCategory<JEIElectroStaticGridCategory.ElectrostaticGridRecipe> {
    protected JEIElectroStaticGridCategory() {
        super(RecipeTypes.ELECTRO_GRID,
                xlate("pneumaticcraft.gui.jei.title.electrostaticGrid"),
                guiHelper().drawableBuilder(Textures.GUI_JEI_ELECTROGRID, 0, 0, 36, 36).setTextureSize(36, 36).build(),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ELECTROSTATIC_COMPRESSOR.get()))
        );
    }

    public static List<ElectrostaticGridRecipe> getAllRecipes() {
        return BuiltInRegistries.BLOCK.getTag(PneumaticCraftTags.Blocks.ELECTROSTATIC_GRID).orElseThrow().stream()
                .map(h -> ElectrostaticGridRecipe.ofBlock(h.value()))
                .toList();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ElectrostaticGridRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 1, 1).addItemStack(new ItemStack(ModBlocks.ELECTROSTATIC_COMPRESSOR));
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addItemStack(recipe.input);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ElectrostaticGridRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(positionalTooltip(mouseX, mouseY, (x, y) -> x >= 18 && y >= 18, "pneumaticcraft.gui.nei.recipe.electrostaticGrid"));
    }

    public record ElectrostaticGridRecipe(ItemStack input) {
        public static ElectrostaticGridRecipe ofBlock(Block block) {
            return new ElectrostaticGridRecipe(new ItemStack(block));
        }
    }
}
