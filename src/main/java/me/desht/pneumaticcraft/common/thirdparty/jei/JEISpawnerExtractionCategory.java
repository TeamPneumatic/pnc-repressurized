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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEISpawnerExtractionCategory extends AbstractPNCCategory<JEISpawnerExtractionCategory.Recipe> {
    public JEISpawnerExtractionCategory() {
        super(ModCategoryUid.SPAWNER_EXTRACTION, Recipe.class,
                xlate("pneumaticcraft.gui.jei.title.spawnerExtraction"),
                guiHelper().createDrawable(Textures.GUI_JEI_SPAWNER_EXTRACTION, 0, 0, 120, 64),
                guiHelper().createDrawableIngredient(new ItemStack(ModBlocks.SPAWNER_EXTRACTOR.get()))
        );
    }

    @Override
    public void setIngredients(Recipe recipe, IIngredients iIngredients) {
        iIngredients.setInputs(VanillaTypes.ITEM, ImmutableList.of(recipe.itemInput, new ItemStack(Blocks.SPAWNER)));
        iIngredients.setOutputLists(VanillaTypes.ITEM, ImmutableList.of(
                recipe.cores,
                Collections.singletonList(recipe.itemOutput2)
        ));
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, Recipe recipe, IIngredients iIngredients) {
        iRecipeLayout.getItemStacks().init(0, true, 52, 2);
        iRecipeLayout.getItemStacks().set(0, iIngredients.getInputs(VanillaTypes.ITEM).get(0));

        iRecipeLayout.getItemStacks().init(1, true, 52, 33);
        iRecipeLayout.getItemStacks().set(1, iIngredients.getInputs(VanillaTypes.ITEM).get(1));

        iRecipeLayout.getItemStacks().init(2, false, 17, 33);
        iRecipeLayout.getItemStacks().set(2, iIngredients.getOutputs(VanillaTypes.ITEM).get(0));

        iRecipeLayout.getItemStacks().init(3, false, 87, 33);
        iRecipeLayout.getItemStacks().set(3, iIngredients.getOutputs(VanillaTypes.ITEM).get(1));
    }

    private static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();
    static {
        ENTITY_TYPES.add(EntityType.ZOMBIE);
        ENTITY_TYPES.add(EntityType.SKELETON);
        ENTITY_TYPES.add(EntityType.CREEPER);
    }
    public static Collection<?> getAllRecipes() {
        List<ItemStack> cores = new ArrayList<>();
        for (EntityType<?> type : ENTITY_TYPES) {
            ItemStack core = new ItemStack(ModItems.SPAWNER_CORE.get());
            ISpawnerCoreStats stats = PneumaticRegistry.getInstance().getItemRegistry().getSpawnerCoreStats(core);
            stats.addAmount(type, 100);
            stats.serialize(core);
            cores.add(core);
        }

        return Collections.singletonList(new Recipe(
                        new ItemStack(ModBlocks.SPAWNER_EXTRACTOR.get()),
                        cores,
                        new ItemStack(ModBlocks.EMPTY_SPAWNER.get())
                )
        );
    }

    static class Recipe {
        final ItemStack itemInput;
        final List<ItemStack> cores;
        final ItemStack itemOutput2;

        Recipe(ItemStack itemInput, List<ItemStack> cores, ItemStack itemOutput2) {
            this.itemInput = itemInput;
            this.cores = cores;
            this.itemOutput2 = itemOutput2;
        }
    }
}
