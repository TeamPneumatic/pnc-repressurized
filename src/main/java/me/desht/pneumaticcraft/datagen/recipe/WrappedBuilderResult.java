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

package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class WrappedBuilderResult implements FinishedRecipe {
    private final FinishedRecipe wrapped;
    private final Supplier<? extends RecipeSerializer<?>> serializer;

    public WrappedBuilderResult(FinishedRecipe wrapped, Supplier<? extends RecipeSerializer<?>> serializer) {
        this.wrapped = wrapped;
        this.serializer = serializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        wrapped.serializeRecipeData(json);
    }

    @Override
    public ResourceLocation getId() {
        return wrapped.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return serializer.get();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return wrapped.serializeAdvancement();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return wrapped.getAdvancementId();
    }
}
