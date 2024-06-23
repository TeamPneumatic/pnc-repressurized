/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.entity.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.EmptyPCBItem;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIEtchingTankCategory extends AbstractPNCCategory<JEIEtchingTankCategory.EtchingTankRecipe> {
    private final IDrawableAnimated progressBar;

    JEIEtchingTankCategory() {
        super(RecipeTypes.ETCHING_TANK,
                xlate(ModBlocks.ETCHING_TANK.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 0, 0, 83, 42),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ETCHING_TANK.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 83, 0, 42, 42);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EtchingTankRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 13)
                .addIngredients(recipe.input);
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 13)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(new FluidStack(ModFluids.ETCHING_ACID.get(), 1000)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 66, 1)
                .addItemStack(recipe.output);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 66, 25)
                .addItemStack(recipe.failed);
    }

    @Override
    public void draw(EtchingTankRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        progressBar.draw(graphics, 20, 0);
    }

    static List<EtchingTankRecipe> getAllRecipes() {
        List<EtchingTankRecipe> recipes = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation itemId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
            String modId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).getNamespace();
            if (item instanceof EmptyPCBItem) {
                ItemStack[] input = new ItemStack[4];
                for (int i = 0; i < input.length; i++) {
                    input[i] = new ItemStack(item);
                    UVLightBoxBlockEntity.setExposureProgress(input[i], 25 + 25 * i);
                }
                ItemStack output;
                ItemStack failed;
                if (itemId.getNamespace().equals(Names.MOD_ID)) {
                    output = new ItemStack(ModItems.UNASSEMBLED_PCB.get());
                    failed = new ItemStack(ModItems.FAILED_PCB.get());
                } else {
                    String itemIdStr = itemId.getPath().replace("_empty_pcb", "");
                    itemIdStr += "_unassembled_pcb";
                    ResourceLocation outputResourceLocation = new ResourceLocation(modId, itemIdStr);
                    Item outputItem = ForgeRegistries.ITEMS.getValue(outputResourceLocation);
                    output = new ItemStack(Objects.requireNonNullElseGet(outputItem, ModItems.UNASSEMBLED_PCB));
                    itemIdStr = itemIdStr.replace("_unassembled_pcb", "");
                    itemIdStr += "_failed_pcb";
                    ResourceLocation failedResourceLocation = new ResourceLocation(modId, itemIdStr);
                    Item failedItem = ForgeRegistries.ITEMS.getValue(failedResourceLocation);
                    failed = new ItemStack(Objects.requireNonNullElseGet(failedItem, ModItems.FAILED_PCB));
                }
                recipes.add(new EtchingTankRecipe(
                        Ingredient.of(input),
                        output,
                        failed)
                );
            }
        }
        return recipes;
    }

    // pseudo-recipe
    record EtchingTankRecipe(Ingredient input, ItemStack output, ItemStack failed) {
    }
}
