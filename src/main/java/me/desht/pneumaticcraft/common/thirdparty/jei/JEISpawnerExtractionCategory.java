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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

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
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.SPAWNER_EXTRACTOR.get()))
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 53, 3).addItemStack(recipe.itemInput);
        builder.addSlot(RecipeIngredientRole.INPUT, 53, 34).addItemStack(new ItemStack(Blocks.SPAWNER));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 18,34).addItemStacks(recipe.cores);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 34).addItemStack(recipe.itemOutput);
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

    record Recipe(ItemStack itemInput, List<ItemStack> cores, ItemStack itemOutput) {
    }
}
