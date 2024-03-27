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

package me.desht.pneumaticcraft.common.recipes.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class AssemblyRecipeImpl extends AssemblyRecipe {
    private final Ingredient input;
    private final ItemStack output;
    private final AssemblyProgramType program;

    public AssemblyRecipeImpl(@Nonnull Ingredient input, @Nonnull ItemStack output, AssemblyProgramType program) {
        this.input = input;
        this.output = output;
        this.program = program;
    }

    @Override
    public Ingredient getInput() {
        return input;
    }

    @Override
    public int getInputAmount() {
        return input.getItems().length > 0 ? input.getItems()[0].getCount() : 0;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public AssemblyProgramType getProgramType() {
        return program;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return input.test(stack) && stack.getCount() >= getInputAmount();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return switch (getProgramType()) {
            case LASER -> ModRecipeSerializers.ASSEMBLY_LASER.get();
            case DRILL -> ModRecipeSerializers.ASSEMBLY_DRILL.get();
            default -> throw new IllegalStateException("invalid program type: " + getProgramType());
        };
    }

    @Override
    public RecipeType<?> getType() {
        return switch (getProgramType()) {
            case DRILL -> ModRecipeTypes.ASSEMBLY_DRILL.get();
            case LASER -> ModRecipeTypes.ASSEMBLY_LASER.get();
            case DRILL_LASER -> ModRecipeTypes.ASSEMBLY_DRILL_LASER.get();
        };
    }

    /**
     * Work out which recipes can be chained.  E.g. if laser recipe makes B from A, and drill recipe makes C from B,
     * then add a synthetic laser/drill recipe to make C from A. Takes into account the number of inputs and outputs
     * from each step.
     *
     * @param drillRecipes all known drill recipes
     * @param laserRecipes all known laser recipes
     * @return a map (recipeId -> recipe) of all synthetic laser/drill recipes
     */
    public static Map<ResourceLocation, RecipeHolder<AssemblyRecipe>> calculateAssemblyChain(
            List<RecipeHolder<AssemblyRecipe>> drillRecipes,
            List<RecipeHolder<AssemblyRecipe>> laserRecipes
    ) {
        Map<ResourceLocation, RecipeHolder<AssemblyRecipe>> drillLaser = new HashMap<>();
        for (RecipeHolder<AssemblyRecipe> h1 : drillRecipes) {
            for (RecipeHolder<AssemblyRecipe> h2 : laserRecipes) {
                AssemblyRecipe r1 = h1.value();
                AssemblyRecipe r2 = h2.value();
                if (r2.getInput().test(r1.getOutput())
                        && r1.getOutput().getCount() % r2.getInputAmount() == 0
                        && r2.getOutput().getMaxStackSize() >= r2.getOutput().getCount() * (r1.getOutput().getCount() / r2.getInputAmount())) {
                    ItemStack output = r2.getOutput().copy();
                    output.setCount(output.getCount() * (r1.getOutput().getCount() / r2.getInputAmount()));
                    ResourceLocation id = RL(h1.id().getPath() + "_" + h2.id().getPath());
                    drillLaser.put(id, new RecipeHolder<>(id, new AssemblyRecipeImpl(r1.getInput(), output, AssemblyProgramType.DRILL_LASER)));
                }
            }
        }
        return drillLaser;
    }

    public static class Serializer<T extends AssemblyRecipe> implements RecipeSerializer<T> {
        private final IFactory<T> factory;
        private final Codec<T> codec;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;

            this.codec = RecordCodecBuilder.create(inst -> inst.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(AssemblyRecipe::getInput),
                    ItemStack.ADVANCEMENT_ICON_CODEC.fieldOf("result").forGetter(AssemblyRecipe::getOutput),
                    Codec.STRING.xmap(str -> Objects.requireNonNull(AssemblyProgramType.valueOf(str)), AssemblyProgramType::name)
                            .fieldOf("program").forGetter(AssemblyRecipe::getProgramType)
            ).apply(inst, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack out = buffer.readItem();
            AssemblyProgramType program = buffer.readEnum(AssemblyProgramType.class);
            return factory.create(input, out, program);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.getInput().toNetwork(buffer);
            buffer.writeItem(recipe.getOutput());
            buffer.writeEnum(recipe.getProgramType());
        }

        public interface IFactory<T extends AssemblyRecipe> {
            T create(@Nonnull Ingredient input, @Nonnull ItemStack output, AssemblyProgramType program);
        }
    }
}
