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

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class AssemblyRecipeImpl extends AssemblyRecipe {
    private final SizedIngredient input;
    private final ItemStack output;
    private final AssemblyProgramType program;

    public AssemblyRecipeImpl(@Nonnull SizedIngredient input, @Nonnull ItemStack output, AssemblyProgramType program) {
        this.input = input;
        this.output = output;
        this.program = program;
    }

    @Override
    public SizedIngredient getInput() {
        return input;
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

    public interface IFactory<T extends AssemblyRecipe> {
        T create(@Nonnull SizedIngredient input, @Nonnull ItemStack output, AssemblyProgramType program);
    }

    public static class Serializer<T extends AssemblyRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(IFactory<T> factory) {
            this.codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(AssemblyRecipe::getInput),
                    ItemStack.CODEC.fieldOf("result").forGetter(AssemblyRecipe::getOutput),
                    AssemblyProgramType.CODEC.validate(Serializer::checkNotDrillAndLaser)
                            .fieldOf("program").forGetter(AssemblyRecipe::getProgramType)
            ).apply(builder, factory::create));
            this.streamCodec = StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, AssemblyRecipe::getInput,
                    ItemStack.STREAM_CODEC, AssemblyRecipe::getOutput,
                    NeoForgeStreamCodecs.enumCodec(AssemblyProgramType.class), AssemblyRecipe::getProgramType,
                    factory::create
            );
        }

        @NotNull
        private static DataResult<AssemblyProgramType> checkNotDrillAndLaser(AssemblyProgramType type) {
            return type == AssemblyProgramType.DRILL_LASER ?
                    DataResult.error(() -> "'drill_laser' may not be used as a recipe type!") :
                    DataResult.success(type);
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
