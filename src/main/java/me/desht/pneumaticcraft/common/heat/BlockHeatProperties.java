package me.desht.pneumaticcraft.common.heat;

import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.heat.HeatRegistrationEvent;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.state.IProperty;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum BlockHeatProperties {
    INSTANCE;

    private static final String BLOCK_HEAT_PROPERTIES = "pneumaticcraft/block_heat_properties";

    private final Map<ResourceLocation, CustomHeatEntry> customHeatEntries = new HashMap<>();

    public static BlockHeatProperties getInstance() {
        return INSTANCE;
    }

    public CustomHeatEntry getCustomHeatEntry(Block block) {
        return customHeatEntries.get(block.getRegistryName());
    }

    /**
     * Get or create a custom heat entry for the given fluid.  If a matching fluid entry already exists, return it.
     * Otherwise, build a defaulted fluid entry based on the fluid's temperature and default thermal properties and
     * add it to the custom entries.
     *
     * @param fluid a fluid
     * @return the custom heat entry, or null if there was a problem adding one
     */
    public CustomHeatEntry getOrCreateCustomHeatEntry(Fluid fluid) {
        Block block = fluid.getDefaultState().getBlockState().getBlock();
        if (block == Blocks.AIR || block == null) return null;

        CustomHeatEntry entry = getCustomHeatEntry(block);
        if (entry == null) {
            entry = buildDefaultFluidEntry(block, fluid);
            customHeatEntries.put(block.getRegistryName(), entry);
        }
        return entry;
    }

    private void clear() {
        customHeatEntries.clear();
    }

    public void register(ResourceLocation id, CustomHeatEntry entry) {
        customHeatEntries.put(id, entry);
    }

    /**
     * For fluids which do have blocks, but don't have a custom heat entry defined, set up a default entry.
     * @param block the fluid block
     * @param fluid the fluid
     * @return a new custom heat entry for this fluid
     */
    private CustomHeatEntry buildDefaultFluidEntry(Block block, Fluid fluid) {
        BlockState transformHot, transformHotFlowing, transformCold, transformColdFlowing;
        int temperature = fluid.getAttributes().getTemperature();
        if (fluid.getAttributes().getTemperature() >= 1300) {  // lava temperature
            transformHot = null;
            transformHotFlowing = null;
            transformCold = Blocks.OBSIDIAN.getDefaultState();
            transformColdFlowing = Blocks.COBBLESTONE.getDefaultState();
        } else {
            transformHot = Blocks.AIR.getDefaultState();
            transformHotFlowing = Blocks.AIR.getDefaultState();
            transformCold = Blocks.ICE.getDefaultState();
            transformColdFlowing = Blocks.SNOW.getDefaultState();
        }
        return new CustomHeatEntry(
                block,
                transformHot, transformHotFlowing,
                transformCold, transformColdFlowing,
                PNCConfig.Common.Heat.defaultFluidHeatCapacity,
                temperature,
                PNCConfig.Common.Heat.defaultFluidThermalResistance,
                Collections.emptyMap()
        );
    }

    public static class ReloadListener extends JsonReloadListener {
        private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

        public ReloadListener() {
            super(GSON, BLOCK_HEAT_PROPERTIES);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonObject> resourceList, IResourceManager resourceManagerIn, IProfiler profilerIn) {
            BlockHeatProperties.getInstance().clear();
            resourceList.forEach((id, json) -> {
                try {
                    CustomHeatEntry entry = CustomHeatEntry.fromJson(json);
                    if (entry != null) {
                        BlockHeatProperties.getInstance().register(entry.getId(), entry);
                    }
                } catch (JsonParseException e) {
                    Log.error("can't load %s: %s", id, e.getMessage());
                    Log.error(ExceptionUtils.getStackTrace(e));
                }
            });

            HeatBehaviourManager.getInstance().reload();

            MinecraftForge.EVENT_BUS.post(new HeatRegistrationEvent(HeatExchangerManager.getInstance()));

            registerDefaultFluidValues();
        }

        private void registerDefaultFluidValues() {
            // add defaulted values for all fluids which don't already have a custom entry
            for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                if (fluid == Fluids.EMPTY) continue;
                Block block = fluid.getDefaultState().getBlockState().getBlock();
                if (block == null || block == Blocks.AIR || BlockHeatProperties.getInstance().getCustomHeatEntry(block) != null) {
                    continue;
                }
                CustomHeatEntry entry = BlockHeatProperties.getInstance().getOrCreateCustomHeatEntry(fluid);
                if (entry == null) {
                    Log.warning("unable to build custom heat entry for fluid %s (block %s) ",
                            fluid.getRegistryName(), block.getRegistryName());
                }
            }
        }
    }

    public static class CustomHeatEntry {
        private final int heatCapacity;
        private final int temperature;
        private final double thermalResistance;
        private final Block block;
        private final BlockState transformHot;
        private final BlockState transformHotFlowing;
        private final BlockState transformCold;
        private final BlockState transformColdFlowing;
        private final ResourceLocation id;
        private final Map<String, String> predicates;
        private final IHeatExchangerLogic logic;

        CustomHeatEntry(Block block,
                        BlockState transformHot, BlockState transformHotFlowing,
                        BlockState transformCold, BlockState transformColdFlowing,
                        int heatCapacity, int temperature, double thermalResistance, Map<String, String> predicates) {
            this.id = block.getRegistryName();
            this.heatCapacity = heatCapacity;
            this.block = block;
            this.transformHot = transformHot;
            this.transformHotFlowing = transformHotFlowing != null ? transformHotFlowing : transformHot;
            this.transformCold = transformCold;
            this.transformColdFlowing = transformColdFlowing != null ? transformColdFlowing : transformCold;
            this.temperature = temperature;
            this.thermalResistance = thermalResistance;
            this.predicates = predicates;
            this.logic = new HeatExchangerLogicConstant(temperature, thermalResistance);
        }

        CustomHeatEntry(Block block, int temperature, double thermalResistance) {
            this (block, null, null, null, null, 0,
                    temperature, thermalResistance, Collections.emptyMap());
        }

        static CustomHeatEntry fromJson(JsonObject json) {
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
                ResourceLocation blockId = new ResourceLocation(JSONUtils.getString(json, "block"));
                if (blockId.toString().equals("minecraft:air")) {
                    throw new JsonSyntaxException("minecraft:air block heat properties may not be changed!");
                }
                block = ForgeRegistries.BLOCKS.getValue(blockId);
                validateBlock(blockId, block);
                fluid = block.getDefaultState().getFluidState().getFluid();  // ok if this is absent
            } else if (json.has("fluid")) {
                ResourceLocation fluidId = new ResourceLocation(JSONUtils.getString(json, "block"));
                fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
                if (fluid == Fluids.EMPTY) {
                    throw new JsonSyntaxException("unknown fluid " + fluidId);
                }
                block = fluid.getDefaultState().getBlockState().getBlock();  // not ok if this is absent!
                validateBlock(fluidId, block);
            } else {
                throw new JsonSyntaxException("heat properties entry must have a \"block\" or \"fluid\" field!");
            }

            if (block == Blocks.AIR) return null;

            // blocks with a total heat capacity will transform into something else if too much heat is added/removed
            int totalHeat = 0;
            if (json.has("heatCapacity")) {
                totalHeat = json.get("heatCapacity").getAsInt();
            } else if (fluid != Fluids.EMPTY) {
                totalHeat = PNCConfig.Common.Heat.defaultFluidHeatCapacity;
            }
            if (totalHeat != 0) {
                transformHot = maybeGetBlock(block, json, "transformHot");
                transformHotFlowing = maybeGetBlock(block, json, "transformHotFlowing");
                transformCold = maybeGetBlock(block, json, "transformCold");
                transformColdFlowing = maybeGetBlock(block, json, "transformColdFlowing");
            }

            int temperature;
            if (json.has("temperature")) {
                temperature = JSONUtils.getInt(json, "temperature");
            } else {
                if (fluid == Fluids.EMPTY) {
                    throw new JsonSyntaxException(block.toString() + ": Non-fluid definitions must have a 'temperature' field!");
                } else {
                    temperature = fluid.getAttributes().getTemperature();
                }
            }

            double thermalResistance;
            if (json.has("thermalResistance")) {
                thermalResistance = json.get("thermalResistance").getAsDouble();
            } else if (fluid == Fluids.EMPTY) {
                thermalResistance = PNCConfig.Common.Heat.defaultBlockThermalResistance;
            } else {
                thermalResistance = PNCConfig.Common.Heat.defaultFluidThermalResistance;
            }

            if (json.has("statePredicate")) {
                json.getAsJsonObject("statePredicate").entrySet().forEach(entry -> {
                    if (block.getStateContainer().getProperty(entry.getKey()) == null) {
                        throw new JsonSyntaxException("unknown blockstate property " + entry.getKey() + " for block" + block.getRegistryName());
                    }
                    predicates.put(entry.getKey(), entry.getValue().getAsString());
                });
            }

            return new CustomHeatEntry(block,
                    transformHot, transformHotFlowing, transformCold, transformColdFlowing,
                    totalHeat, temperature, thermalResistance, predicates
            );
        }

        public IHeatExchangerLogic getLogic() {
            return logic;
        }

        boolean testPredicates(BlockState state) {
            if (predicates.isEmpty()) return true;
            for (Map.Entry<String, String> entry : predicates.entrySet()) {
                IProperty<?> iproperty = state.getBlock().getStateContainer().getProperty(entry.getKey());
                if (iproperty == null) {
                    return false;
                }
                Comparable<?> comparable = iproperty.parseValue(entry.getValue()).orElse(null);
                // looks dubious, but should be OK, at least for boolean/enum/integer
                //noinspection EqualsBetweenInconvertibleTypes
                if (comparable == null || state.get(iproperty) != comparable) {
                    return false;
                }
            }
            return true;
        }

        private static void validateBlock(ResourceLocation blockId, Block block) {
            if (block == Blocks.AIR) {
                String mod = blockId.getNamespace();
                if (ModList.get().isLoaded(mod)) {
                    throw new JsonSyntaxException("unknown block id: " + blockId.toString());
                } else {
                    // not really an error
                    Log.info("ignoring heat properties for %s: mod '%s' not loaded", blockId, mod);
                }
            }
        }

        private static BlockState parseBlockState(String str) {
            try {
                BlockStateParser parser = (new BlockStateParser(new StringReader(str), false)).parse(false);
                return parser.getState();
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(String.format("invalid blockstate [%s] - %s", str, e.getMessage()));
            }
        }

        private static BlockState maybeGetBlock(Block b, JsonObject json, String field) {
            if (!json.has(field)) return null;

            JsonObject sub = json.get(field).getAsJsonObject();
            if (sub.has("block")) {
                return parseBlockState(JSONUtils.getString(sub, "block"));
            } else if (sub.has("fluid")) {
                ResourceLocation fluidId = new ResourceLocation(JSONUtils.getString(sub, "fluid"));
                if (ForgeRegistries.FLUIDS.containsKey(fluidId)) {
                    return ForgeRegistries.FLUIDS.getValue(fluidId).getDefaultState().getBlockState();
                } else {
                    throw new JsonSyntaxException(String.format("unknown fluid '%s' for field '%s' in block '%s'", fluidId, field, b.getRegistryName()));
                }
            } else {
                throw new JsonSyntaxException(String.format("block %s must have either a 'block' or 'fluid' section!", b.getRegistryName()));
            }
        }

        public int getHeatCapacity() {
            return heatCapacity;
        }

        public int getTemperature() {
            return temperature;
        }

        public double getThermalResistance() {
            return thermalResistance;
        }

        public Block getBlock() {
            return block;
        }

        public BlockState getTransformHot() {
            return transformHot;
        }

        public BlockState getTransformCold() {
            return transformCold;
        }

        public BlockState getTransformHotFlowing() {
            return transformHotFlowing;
        }

        public BlockState getTransformColdFlowing() {
            return transformColdFlowing;
        }

        public ResourceLocation getId() {
            return id;
        }

    }
}
