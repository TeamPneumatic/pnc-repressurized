/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.recipes.special;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Just like a regular shaped recipe, but any air in any input ingredients is added up and put into the output item.
 */
public class ShapedPressurizableRecipe extends WrappedShapedRecipe {
    public ShapedPressurizableRecipe(ShapedRecipe wrapped) {
        super(wrapped);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack newOutput = this.getResultItem(registryAccess).copy();

        IOHelper.getCap(newOutput, PNCCapabilities.AIR_HANDLER_ITEM).ifPresent(outputHandler -> {
            int totalAir = 0;
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack stack = inv.getItem(i);
                totalAir += IOHelper.getCap(stack, PNCCapabilities.AIR_HANDLER_ITEM)
                        .map(IAirHandler::getAir).orElse(0);
            }
            outputHandler.addAir(totalAir);
        });

        return newOutput;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SHAPED_PRESSURIZABLE.get();
    }

    public static class Serializer implements RecipeSerializer<ShapedPressurizableRecipe> {
        public static final Codec<ShapedPressurizableRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(
                ShapedPressurizableRecipe::new, ShapedPressurizableRecipe::getWrapped
        );

        @Override
        public Codec<ShapedPressurizableRecipe> codec() {
            return CODEC;
        }

        @Override
        public ShapedPressurizableRecipe fromNetwork(FriendlyByteBuf buf) {
            return new ShapedPressurizableRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShapedPressurizableRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe.wrapped);
        }
    }
}
