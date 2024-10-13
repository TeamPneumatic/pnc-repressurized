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

package me.desht.pneumaticcraft.api.crafting.recipe;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Map;
import java.util.Optional;

/**
 * Recipes which define the heat properties of a block; its temperature, thermal resistance, heat capacity, and the
 * blocks it can turn into when too much heat is added or removed via the PneumaticCraft heat system.
 */
public abstract class HeatPropertiesRecipe extends PneumaticCraftRecipe {
    /**
     * Get the block, which is effectively the input for this recipe
     * @return the block
     */
    public abstract Block getBlock();

    /**
     * Get the blockstate for this entry. This is not necessarily the block's default state (e.g. a vanilla campfire
     * only has heat when it's lit, which is a boolean blockstate property)
     *
     * @return the blockstate
     */
    public abstract BlockState getBlockState();

    /**
     * Get the heat capacity for this block. This is the amount of heat which can be gained or lost before it
     * transforms into some other block.
     *
     * @return the heat capacity, or {@code Optional.empty()} if this block never transforms
     */
    public abstract Optional<Integer> getHeatCapacity();

    /**
     * The block's temperature.  For fluid blocks, this will be temperature of the fluid, which is defined by the
     * mod which registered that fluid (or 1300K for lava and 300K for water)
     *
     * @return the block's temperature
     */
    public abstract int getTemperature();

    /**
     * The block's thermal resistance, which defines how quickly heat is added or lost. Higher resistances mean a
     * slower transfer of heat in or out of the block. You can return {@code Optional.empty()} here if the block
     * should just use the default resistance from the mod configuration (see <em>blockThermalResistance</em> and
     * <em>fluidThermalResistance</em> config settings).
     *
     * @return the thermal resistance
     */
    public abstract Optional<Double> getThermalResistance();

    public abstract Transforms getTransforms();

    /**
     * Get the blockstate which the input will transform to if too much heat is added to it. This may be empty if there
     * is no hot transformation.
     *
     * @return a new blockstate
     */
    public abstract Optional<BlockState> getTransformHot();

    /**
     * Get the blockstate which the input will transform to if too much heat is removed from it. This may be empty if
     * there is no cold transformation.
     *
     * @return a new blockstate
     */
    public abstract Optional<BlockState> getTransformCold();

    /**
     * Same as {@link #getTransformHot()} but for flowing variants of the input block, when it is a fluid.
     *
     * @return a new blockstate
     */
    public abstract Optional<BlockState> getTransformHotFlowing();

    /**
     * Same as {@link #getTransformCold()} but for flowing variants of the input block, when it is a fluid.
     *
     * @return a new blockstate
     */
    public abstract Optional<BlockState> getTransformColdFlowing();

    /**
     * Get a heat exchanger logic object for this recipe. This is mainly a convenience to get the associated temperature
     * and thermal resistance.
     *
     * @return a heat exchanger logic
     */
    public abstract IHeatExchangerLogic getLogic();

    /**
     * Check if this recipe's input matches the supplied blockstate. See also {@link #getBlockStatePredicates()}.
     *
     * @param state the blockstate to test against
     * @return true if this recipe matches the supplied blockstate, false otherwise
     */
    public abstract boolean matchState(BlockState state);

    /**
     * Get the blockstate predicates for this recipe. All of these predicates must match for {@link #matchState(BlockState)}
     * to succeed.
     * E.g. for a lit campfire, this map would be { "lit" = "true" }
     *
     * @return a map of blockstate properties to their required values
     */
    public abstract Map<String,String> getBlockStatePredicates();

    /**
     * Get a translation key for a player-friendly description of this recipe. This could be empty; if not empty,
     * then it should be used by recipe display systems like JEI as a supplementary description for this recipe,
     * appending to the result of {@link #getInputDisplayName()}.
     *
     * @return a translation key, or the empty string for no supplementary text
     */
    public abstract String getDescriptionKey();

    /**
     * Get a player-friendly display name for the input block, for use in recipe display systems like JEI.
     *
     * @return some displayable text
     */
    public Component getInputDisplayName() {
        return getBlock() instanceof LiquidBlock liq ?
                new FluidStack(liq.fluid, FluidType.BUCKET_VOLUME).getHoverName() :
                new ItemStack(getBlock()).getHoverName();
    }

    private static final Codec<BlockState> BLOCKSTATE_STRING_CODEC = Codec.STRING.comapFlatMap(
            string -> {
                try {
                    return DataResult.success(BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), string, false).blockState());
                } catch (CommandSyntaxException e) {
                    return DataResult.error(() -> "invalid blockstate definition: " + string);
                }
            },
            BlockStateParser::serialize
    );

    public record Transforms(Optional<BlockState> hot, Optional<BlockState> cold,
                             Optional<BlockState> hotFlowing, Optional<BlockState> coldFlowing) {
        public static final Codec<Transforms> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BLOCKSTATE_STRING_CODEC.optionalFieldOf("hot").forGetter(Transforms::hot),
                BLOCKSTATE_STRING_CODEC.optionalFieldOf("cold").forGetter(Transforms::cold),
                BLOCKSTATE_STRING_CODEC.optionalFieldOf("hot_flowing").forGetter(Transforms::hotFlowing),
                BLOCKSTATE_STRING_CODEC.optionalFieldOf("cold_flowing").forGetter(Transforms::coldFlowing)
        ).apply(builder, Transforms::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Transforms> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY)), Transforms::hot,
                ByteBufCodecs.optional(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY)), Transforms::cold,
                ByteBufCodecs.optional(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY)), Transforms::hotFlowing,
                ByteBufCodecs.optional(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY)), Transforms::coldFlowing,
                Transforms::new
        );

        public static final Transforms NONE = new Transforms(
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty()
        );
    }
}
