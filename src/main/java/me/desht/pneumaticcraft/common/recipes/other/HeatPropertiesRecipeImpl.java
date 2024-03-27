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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicConstant;
import me.desht.pneumaticcraft.common.network.PacketUtil;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// codecs mean optional fields and params!
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class HeatPropertiesRecipeImpl extends HeatPropertiesRecipe {
    private final Block block;
    private final BlockState inputState;
    private final Optional<BlockState> transformHot;
    private final Optional<BlockState> transformHotFlowing;
    private final Optional<BlockState> transformCold;
    private final Optional<BlockState> transformColdFlowing;
    private final Map<String, String> predicates;
    private final Optional<Integer> heatCapacity;
    private final int temperature;
    private final Optional<Double> thermalResistance;
    private final String descriptionKey;
    private final HeatExchangerLogicConstant logic;

    public HeatPropertiesRecipeImpl(Block block,
                                    Optional<BlockState> transformHot, Optional<BlockState> transformHotFlowing,
                                    Optional<BlockState> transformCold, Optional<BlockState> transformColdFlowing,
                                    Optional<Integer> heatCapacity, int temperature, Optional<Double> thermalResistance,
                                    Map<String, String> predicates, String descriptionKey)
    {
        super();

        this.block = block;
        this.transformHot = transformHot;
        this.transformHotFlowing = transformHotFlowing;
        this.transformCold = transformCold;
        this.transformColdFlowing = transformColdFlowing;
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
    public Optional<BlockState> getTransformHot() {
        return transformHot;
    }

    @Override
    public Optional<BlockState> getTransformCold() {
        return transformCold;
    }

    @Override
    public Optional<BlockState> getTransformHotFlowing() {
        return transformHotFlowing;
    }

    @Override
    public Optional<BlockState> getTransformColdFlowing() {
        return transformColdFlowing;
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

    public static class Serializer<T extends HeatPropertiesRecipe> implements RecipeSerializer<T> {
        private static final Codec<Map<String,String>> PREDICATES = Codec.unboundedMap(Codec.STRING, Codec.STRING);
        private static final Codec<BlockState> BLOCKSTATE_STRING = Codec.STRING.comapFlatMap(
                str -> {
                    try {
                        var result = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), str, false);
                        return DataResult.success(result.blockState());
                    } catch (CommandSyntaxException e) {
                        return DataResult.error(() -> "can't parse blockstate " + str + ": " + e.getMessage());
                    }
                },
                BlockStateParser::serialize
        );

        private final IFactory<T> factory;
        private final Codec<T> codec;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
            this.codec = RecordCodecBuilder.create(builder -> builder.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(HeatPropertiesRecipe::getBlock),
                    BLOCKSTATE_STRING.optionalFieldOf("transformHot").forGetter(HeatPropertiesRecipe::getTransformHot),
                    BLOCKSTATE_STRING.optionalFieldOf("transformHotFlowing").forGetter(HeatPropertiesRecipe::getTransformHotFlowing),
                    BLOCKSTATE_STRING.optionalFieldOf("transformCold").forGetter(HeatPropertiesRecipe::getTransformCold),
                    BLOCKSTATE_STRING.optionalFieldOf("transformColdFlowing").forGetter(HeatPropertiesRecipe::getTransformColdFlowing),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("heatCapacity").forGetter(HeatPropertiesRecipe::getHeatCapacity),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("temperature", 0).forGetter(HeatPropertiesRecipe::getTemperature),
                    Codec.DOUBLE.optionalFieldOf("thermalResistance").forGetter(HeatPropertiesRecipe::getThermalResistance),
                    PREDICATES.optionalFieldOf("predicates", Map.of()).forGetter(HeatPropertiesRecipe::getBlockStatePredicates),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(HeatPropertiesRecipe::getDescriptionKey)
            ).apply(builder, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

//        @Override
//        public T fromJson(ResourceLocation recipeId, JsonObject json) {
//            BlockState transformHot = null;
//            BlockState transformHotFlowing = null;
//            BlockState transformCold = null;
//            BlockState transformColdFlowing = null;
//            Map<String, String> predicates = new HashMap<>();
//
//            Block block;
//            Fluid fluid;
//
//            if (json.has("block") && json.has("fluid")) {
//                throw new JsonSyntaxException("heat properties entry must have only one of \"block\" or \"fluid\" fields!");
//            }
//            if (json.has("block")) {
//                ResourceLocation blockId = new ResourceLocation(GsonHelper.getAsString(json, "block"));
//                if (blockId.toString().equals("minecraft:air")) {
//                    throw new JsonSyntaxException("minecraft:air block heat properties may not be changed!");
//                }
//                block = ForgeRegistries.BLOCKS.getValue(blockId);
//                if (!validateBlock(blockId, block)) {
//                    return null;
//                }
//                fluid = Objects.requireNonNull(block).defaultBlockState().getFluidState().getType();  // ok if this is absent
//            } else if (json.has("fluid")) {
//                ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
//                fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
//                if (!validateFluid(fluidId, fluid)) {
//                    return null;
//                }
//                block = Objects.requireNonNull(fluid).defaultFluidState().createLegacyBlock().getBlock();  // not ok if this is absent!
//                if (!validateBlock(fluidId, block)) {
//                    return null;
//                }
//            } else {
//                throw new JsonSyntaxException("heat properties entry must have a \"block\" or \"fluid\" field!");
//            }
//
//            // blocks with a total heat capacity will transform into something else if too much heat is added/removed
//            int totalHeat = 0;
//            if (json.has("heatCapacity")) {
//                totalHeat = json.get("heatCapacity").getAsInt();
//            } else if (fluid != Fluids.EMPTY) {
//                totalHeat = ConfigHelper.common().heat.defaultFluidHeatCapacity.get();
//            }
//            if (totalHeat != 0) {
//                transformHot = maybeGetBlockState(block, json, "transformHot");
//                transformHotFlowing = maybeGetBlockState(block, json, "transformHotFlowing");
//                transformCold = maybeGetBlockState(block, json, "transformCold");
//                transformColdFlowing = maybeGetBlockState(block, json, "transformColdFlowing");
//            }
//
//            int temperature;
//            if (json.has("temperature")) {
//                temperature = GsonHelper.getAsInt(json, "temperature");
//            } else {
//                if (fluid == Fluids.EMPTY) {
//                    throw new JsonSyntaxException(block + ": Non-fluid definitions must have a 'temperature' field!");
//                } else {
//                    temperature = fluid.getFluidType().getTemperature();
//                }
//            }
//
//            double thermalResistance;
//            if (json.has("thermalResistance")) {
//                thermalResistance = json.get("thermalResistance").getAsDouble();
//            } else if (fluid == Fluids.EMPTY) {
//                thermalResistance = ConfigHelper.common().heat.blockThermalResistance.get();
//            } else {
//                thermalResistance = ConfigHelper.common().heat.fluidThermalResistance.get();
//            }
//
//            if (json.has("statePredicate")) {
//                json.getAsJsonObject("statePredicate").entrySet().forEach(entry -> {
//                    if (block.getStateDefinition().getProperty(entry.getKey()) == null) {
//                        throw new JsonSyntaxException("unknown blockstate property " + entry.getKey() + " for block" + block);
//                    }
//                    predicates.put(entry.getKey(), entry.getValue().getAsString());
//                });
//            }
//
//            String descriptionKey = GsonHelper.getAsString(json, "description", "");
//
//            return factory.create(recipeId, block,
//                    transformHot, transformHotFlowing, transformCold, transformColdFlowing,
//                    totalHeat, temperature, thermalResistance, predicates, descriptionKey
//            );
//        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            Block block = buffer.readById(BuiltInRegistries.BLOCK);
            Optional<BlockState> transformHot = PacketUtil.readOptionalBlockState(buffer);
            Optional<BlockState> transformCold = PacketUtil.readOptionalBlockState(buffer);
            Optional<BlockState> transformHotFlowing = PacketUtil.readOptionalBlockState(buffer);
            Optional<BlockState> transformColdFlowing = PacketUtil.readOptionalBlockState(buffer);
            Map<String,String> predicates = buffer.readMap(FriendlyByteBuf::readUtf, (FriendlyByteBuf.Reader<String>) FriendlyByteBuf::readUtf);
            int temperature = buffer.readInt();
            Optional<Integer> heatCapacity = buffer.readOptional(FriendlyByteBuf::readInt);
            Optional<Double> thermalResistance = buffer.readOptional(FriendlyByteBuf::readDouble);
            String descriptionKey = buffer.readUtf();

            return factory.create(block,
                    transformHot, transformHotFlowing, transformCold, transformColdFlowing,
                    heatCapacity, temperature, thermalResistance, predicates, descriptionKey
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            buffer.writeId(BuiltInRegistries.BLOCK, recipe.getBlock());
            PacketUtil.writeOptionalBlockState(buffer, recipe.getTransformHot());
            PacketUtil.writeOptionalBlockState(buffer, recipe.getTransformCold());
            PacketUtil.writeOptionalBlockState(buffer, recipe.getTransformHotFlowing());
            PacketUtil.writeOptionalBlockState(buffer, recipe.getTransformColdFlowing());
            buffer.writeMap(recipe.getBlockStatePredicates(), FriendlyByteBuf::writeUtf, (FriendlyByteBuf.Writer<String>) FriendlyByteBuf::writeUtf);
            buffer.writeInt(recipe.getTemperature());
            buffer.writeOptional(recipe.getHeatCapacity(), FriendlyByteBuf::writeInt);
            buffer.writeOptional(recipe.getThermalResistance(), FriendlyByteBuf::writeDouble);
            buffer.writeUtf(recipe.getDescriptionKey());
        }

        public interface IFactory <T extends HeatPropertiesRecipe> {
            T create(Block block,
                     Optional<BlockState> transformHot, Optional<BlockState> transformHotFlowing,
                     Optional<BlockState> transformCold, Optional<BlockState> transformColdFlowing,
                     Optional<Integer> heatCapacity, int temperature, Optional<Double> thermalResistance,
                     Map<String, String> predicates, String descriptionKey);
        }
    }
}
