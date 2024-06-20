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

package me.desht.pneumaticcraft.common.recipes.other;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicConstant;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class HeatPropertiesRecipeImpl extends HeatPropertiesRecipe {
    private final Block block;
    private final BlockState inputState;
    private final Transforms transforms;
    private final Map<String, String> predicates;
    private final Optional<Integer> heatCapacity;
    private final int temperature;
    private final Optional<Double> thermalResistance;
    private final String descriptionKey;
    private final HeatExchangerLogicConstant logic;

    public HeatPropertiesRecipeImpl(Block block, Transforms transforms,
                                    Optional<Integer> heatCapacity, int temperature, Optional<Double> thermalResistance,
                                    Map<String, String> predicates, String descriptionKey)
    {
        super();

        this.block = block;
        this.transforms = transforms;
        this.predicates = ImmutableMap.copyOf(predicates);
        this.heatCapacity = heatCapacity;
        this.temperature = temperature;
        this.thermalResistance = thermalResistance;
        this.descriptionKey = descriptionKey;
        this.logic = new HeatExchangerLogicConstant(temperature, thermalResistance.orElse(0.0));
        this.inputState = makeInputState();
    }

    private BlockState makeInputState() {
        if (!predicates.isEmpty()) {
            List<String> l = predicates.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .toList();
            try {
                String regName = PneumaticCraftUtils.getRegistryName(block).orElseThrow().toString();
                String str = regName + "[" + String.join(",", l) + "]";
                BlockStateParser.BlockResult res = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), str, false);
                return res.blockState();
            } catch (CommandSyntaxException e) {
                return block.defaultBlockState();
            }
        } else {
            return block.defaultBlockState();
        }
    }

    @Override
    public Optional<Integer> getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    @Override
    public Optional<Double> getThermalResistance() {
        return thermalResistance;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockState getBlockState() {
        return inputState;
    }

    @Override
    public Transforms getTransforms() {
        return transforms;
    }

    @Override
    public Optional<BlockState> getTransformHot() {
        return transforms.hot();
    }

    @Override
    public Optional<BlockState> getTransformCold() {
        return transforms.cold();
    }

    @Override
    public Optional<BlockState> getTransformHotFlowing() {
        return transforms.hotFlowing();
    }

    @Override
    public Optional<BlockState> getTransformColdFlowing() {
        return transforms.coldFlowing();
    }

    @Override
    public IHeatExchangerLogic getLogic() {
        return logic;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.HEAT_PROPERTIES.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.BLOCK_HEAT_PROPERTIES.get();
    }

    @Override
    public boolean matchState(BlockState state) {
        if (predicates.isEmpty()) return true;
        for (Map.Entry<String, String> entry : predicates.entrySet()) {
            Property<?> iproperty = state.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (iproperty == null) {
                return false;
            }
            Comparable<?> comparable = iproperty.getValue(entry.getValue()).orElse(null);
            // looks weird, but should be OK, at least for boolean/enum/integer properties
            if (comparable == null || state.getValue(iproperty) != comparable) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, String> getBlockStatePredicates() {
        return predicates;
    }

    @Override
    public String getDescriptionKey() {
        return descriptionKey;
    }

    public interface IFactory <T extends HeatPropertiesRecipe> {
        T create(Block block, Transforms transforms,
                 Optional<Integer> heatCapacity, int temperature, Optional<Double> thermalResistance,
                 Map<String, String> predicates, String descriptionKey);
    }

    public static class Serializer<T extends HeatPropertiesRecipe> implements RecipeSerializer<T> {
        private static final Codec<Map<String,String>> PREDICATES_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING);

        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(IFactory<T> factory) {
            this.codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(HeatPropertiesRecipe::getBlock),
                    Transforms.CODEC.fieldOf("transforms").forGetter(HeatPropertiesRecipe::getTransforms),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("heatCapacity").forGetter(HeatPropertiesRecipe::getHeatCapacity),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("temperature", 0).forGetter(HeatPropertiesRecipe::getTemperature),
                    Codec.DOUBLE.optionalFieldOf("thermalResistance").forGetter(HeatPropertiesRecipe::getThermalResistance),
                    PREDICATES_CODEC.optionalFieldOf("predicates", Map.of()).forGetter(HeatPropertiesRecipe::getBlockStatePredicates),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(HeatPropertiesRecipe::getDescriptionKey)
            ).apply(builder, factory::create));
            this.streamCodec = NeoForgeStreamCodecs.composite(
                    ByteBufCodecs.fromCodec(BuiltInRegistries.BLOCK.byNameCodec()), HeatPropertiesRecipe::getBlock,
                    Transforms.STREAM_CODEC, HeatPropertiesRecipe::getTransforms,
                    ByteBufCodecs.optional(ByteBufCodecs.INT), HeatPropertiesRecipe::getHeatCapacity,
                    ByteBufCodecs.INT, HeatPropertiesRecipe::getTemperature,
                    ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), HeatPropertiesRecipe::getThermalResistance,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), HeatPropertiesRecipe::getBlockStatePredicates,
                    ByteBufCodecs.STRING_UTF8, HeatPropertiesRecipe::getDescriptionKey,
                    factory::create
            );
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
