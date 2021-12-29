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
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicConstant;
import me.desht.pneumaticcraft.common.network.PacketUtil;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class HeatPropertiesRecipeImpl extends HeatPropertiesRecipe {
    private final Block block;
    private final BlockState inputState;
    private final BlockState transformHot;
    private final BlockState transformHotFlowing;
    private final BlockState transformCold;
    private final BlockState transformColdFlowing;
    private final Map<String, String> predicates;
    private final IHeatExchangerLogic logic;
    private final int heatCapacity;
    private final int temperature;
    private final double thermalResistance;
    private final String descriptionKey;

    public HeatPropertiesRecipeImpl(ResourceLocation id, Block block,
                                    BlockState transformHot, BlockState transformHotFlowing,
                                    BlockState transformCold, BlockState transformColdFlowing,
                                    int heatCapacity, int temperature, double thermalResistance,
                                    Map<String, String> predicates, String descriptionKey)
    {
        super(id);

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
        this.logic = new HeatExchangerLogicConstant(temperature, thermalResistance);
        this.inputState = makeInputState();
    }

    public HeatPropertiesRecipeImpl(ResourceLocation id, Block block, int temperature, double thermalResistance) {
        this(id, block, null, null, null, null, 0, temperature, thermalResistance, Collections.emptyMap(), "");
    }

    private BlockState makeInputState() {
        if (!predicates.isEmpty()) {
            List<String> l = predicates.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.toList());
            BlockStateParser parser;
            try {
                String str = block.getRegistryName().toString() + "[" + String.join(",", l) + "]";
                parser = (new BlockStateParser(new StringReader(str), false)).parse(false);
            } catch (CommandSyntaxException e) {
                return block.defaultBlockState();
            }
            return parser.getState();
        } else {
            return block.defaultBlockState();
        }
    }

    @Override
    public int getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    @Override
    public double getThermalResistance() {
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
    public BlockState getTransformHot() {
        return transformHot;
    }

    @Override
    public BlockState getTransformCold() {
        return transformCold;
    }

    @Override
    public BlockState getTransformHotFlowing() {
        return transformHotFlowing;
    }

    @Override
    public BlockState getTransformColdFlowing() {
        return transformColdFlowing;
    }

    @Override
    public IHeatExchangerLogic getLogic() {
        return logic;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeRegistryId(block);
        PacketUtil.writeNullableBlockState(buffer, transformHot);
        PacketUtil.writeNullableBlockState(buffer, transformCold);
        PacketUtil.writeNullableBlockState(buffer, transformHotFlowing);
        PacketUtil.writeNullableBlockState(buffer, transformColdFlowing);
        buffer.writeVarInt(predicates.size());
        predicates.forEach((key, val) -> {
            buffer.writeUtf(key);
            buffer.writeUtf(val);
        });
        buffer.writeInt(temperature);
        buffer.writeInt(heatCapacity);
        buffer.writeDouble(thermalResistance);
        buffer.writeUtf(descriptionKey);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.HEAT_PROPERTIES.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return PneumaticCraftRecipeType.HEAT_PROPERTIES;
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

    public static class Serializer<T extends HeatPropertiesRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            BlockState transformHot = null;
            BlockState transformHotFlowing = null;
            BlockState transformCold = null;
            BlockState transformColdFlowing = null;
            Map<String, String> predicates = new HashMap<>();

            Block block;
            Fluid fluid;

            if (json.has("block") && json.has("fluid")) {
                throw new JsonSyntaxException("heat properties entry must have only one of \"block\" or \"fluid\" fields!");
            }
            if (json.has("block")) {
                ResourceLocation blockId = new ResourceLocation(JSONUtils.getAsString(json, "block"));
                if (blockId.toString().equals("minecraft:air")) {
                    throw new JsonSyntaxException("minecraft:air block heat properties may not be changed!");
                }
                if (!ModList.get().isLoaded(blockId.getNamespace())) {
                    Log.info("ignoring heat properties for block %s: mod not loaded", blockId);
                    return null;
                }
                block = ForgeRegistries.BLOCKS.getValue(blockId);
                validateBlock(blockId, block);
                fluid = Objects.requireNonNull(block).defaultBlockState().getFluidState().getType();  // ok if this is absent
            } else if (json.has("fluid")) {
                ResourceLocation fluidId = new ResourceLocation(JSONUtils.getAsString(json, "fluid"));
                if (!ModList.get().isLoaded(fluidId.getNamespace())) {
                    Log.info("ignoring heat properties for fluid %s: mod not loaded", fluidId);
                    return null;
                }
                fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
                if (fluid == null || fluid == Fluids.EMPTY) {
                    throw new JsonSyntaxException("unknown fluid " + fluidId);
                }
                block = fluid.defaultFluidState().createLegacyBlock().getBlock();  // not ok if this is absent!
                validateBlock(fluidId, block);
            } else {
                throw new JsonSyntaxException("heat properties entry must have a \"block\" or \"fluid\" field!");
            }

            // blocks with a total heat capacity will transform into something else if too much heat is added/removed
            int totalHeat = 0;
            if (json.has("heatCapacity")) {
                totalHeat = json.get("heatCapacity").getAsInt();
            } else if (fluid != Fluids.EMPTY) {
                totalHeat = ConfigHelper.common().heat.defaultFluidHeatCapacity.get();
            }
            if (totalHeat != 0) {
                transformHot = maybeGetBlockState(block, json, "transformHot");
                transformHotFlowing = maybeGetBlockState(block, json, "transformHotFlowing");
                transformCold = maybeGetBlockState(block, json, "transformCold");
                transformColdFlowing = maybeGetBlockState(block, json, "transformColdFlowing");
            }

            int temperature;
            if (json.has("temperature")) {
                temperature = JSONUtils.getAsInt(json, "temperature");
            } else {
                if (fluid == Fluids.EMPTY) {
                    throw new JsonSyntaxException(block + ": Non-fluid definitions must have a 'temperature' field!");
                } else {
                    temperature = fluid.getAttributes().getTemperature();
                }
            }

            double thermalResistance;
            if (json.has("thermalResistance")) {
                thermalResistance = json.get("thermalResistance").getAsDouble();
            } else if (fluid == Fluids.EMPTY) {
                thermalResistance = ConfigHelper.common().heat.blockThermalResistance.get();
            } else {
                thermalResistance = ConfigHelper.common().heat.fluidThermalResistance.get();
            }

            if (json.has("statePredicate")) {
                json.getAsJsonObject("statePredicate").entrySet().forEach(entry -> {
                    if (block.getStateDefinition().getProperty(entry.getKey()) == null) {
                        throw new JsonSyntaxException("unknown blockstate property " + entry.getKey() + " for block" + block.getRegistryName());
                    }
                    predicates.put(entry.getKey(), entry.getValue().getAsString());
                });
            }

            String descriptionKey = JSONUtils.getAsString(json, "description", "");

            return factory.create(recipeId, block,
                    transformHot, transformHotFlowing, transformCold, transformColdFlowing,
                    totalHeat, temperature, thermalResistance, predicates, descriptionKey
            );
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            Block block = buffer.readRegistryId();
            BlockState transformHot = PacketUtil.readNullableBlockState(buffer);
            BlockState transformCold = PacketUtil.readNullableBlockState(buffer);
            BlockState transformHotFlowing = PacketUtil.readNullableBlockState(buffer);
            BlockState transformColdFlowing = PacketUtil.readNullableBlockState(buffer);
            ImmutableMap.Builder<String,String> predBuilder = ImmutableMap.builder();
            int nPredicates = buffer.readVarInt();
            for (int i = 0; i < nPredicates; i++) {
                predBuilder.put(buffer.readUtf(), buffer.readUtf());
            }
            int temperature = buffer.readInt();
            int heatCapacity = buffer.readInt();
            double thermalResistance = buffer.readDouble();
            String descriptionKey = buffer.readUtf();

            return factory.create(recipeId, block,
                    transformHot, transformHotFlowing, transformCold, transformColdFlowing,
                    heatCapacity, temperature, thermalResistance, predBuilder.build(), descriptionKey
            );
        }

        @Override
        public void toNetwork(PacketBuffer buffer, T recipe) {
            recipe.write(buffer);
        }

        private void validateBlock(ResourceLocation blockId, Block block) {
            if (block == null || block == Blocks.AIR) {
                throw new JsonSyntaxException("unknown block id: " + blockId.toString());
            }
        }

        private BlockState parseBlockState(String str) {
            try {
                BlockStateParser parser = (new BlockStateParser(new StringReader(str), false)).parse(false);
                return parser.getState();
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(String.format("invalid blockstate [%s] - %s", str, e.getMessage()));
            }
        }

        private BlockState maybeGetBlockState(Block b, JsonObject json, String field) {
            if (!json.has(field)) return null;

            JsonObject sub = json.get(field).getAsJsonObject();
            if (sub.has("block")) {
                return parseBlockState(JSONUtils.getAsString(sub, "block"));
            } else if (sub.has("fluid")) {
                ResourceLocation fluidId = new ResourceLocation(JSONUtils.getAsString(sub, "fluid"));
                if (ForgeRegistries.FLUIDS.containsKey(fluidId)) {
                    //noinspection ConstantConditions
                    return ForgeRegistries.FLUIDS.getValue(fluidId).defaultFluidState().createLegacyBlock();
                } else {
                    throw new JsonSyntaxException(String.format("unknown fluid '%s' for field '%s' in block '%s'", fluidId, field, b.getRegistryName()));
                }
            } else {
                throw new JsonSyntaxException(String.format("block %s must have either a 'block' or 'fluid' section!", b.getRegistryName()));
            }
        }

        public interface IFactory <T extends HeatPropertiesRecipe> {
            T create(ResourceLocation id, Block block,
                     BlockState transformHot, BlockState transformHotFlowing,
                     BlockState transformCold, BlockState transformColdFlowing,
                     int heatCapacity, int temperature, double thermalResistance,
                     Map<String, String> predicates, String descriptionKey);
        }
    }
}
