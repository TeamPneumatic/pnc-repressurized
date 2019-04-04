package me.desht.pneumaticcraft.common.config;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.gson.*;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockHeatPropertiesConfig extends JsonConfig {
    private static final String HEATBLOCK_CFG_ASSET = "/assets/pneumaticcraft/config/BlockHeatProperties.json";

    public static final BlockHeatPropertiesConfig INSTANCE = new BlockHeatPropertiesConfig();

    private final Map<String, CustomHeatEntry> customHeatEntries = new HashMap<>();
    private final Set<Block> ignoreVariants = new HashSet<>();

    private static double defaultBlockThermalResistance = 500.0;
    private static double defaultFluidThermalResistance = 150.0;
    private static int defaultFluidTotalHeat = 10000;
    public static double ambientTempBiomeModifier = 0;
    public static double ambientTempHeightModifier = 0;

    private BlockHeatPropertiesConfig() {
        super(false);
    }

    @Override
    public void preInit(File file) throws IOException {
        super.preInit(file);

        // TODO this part shouldn't be necessary with data packs in 1.13
        mergeConfigs();
    }

    /**
     * Copy any definitions from internal resource file that aren't in the saved file, but don't modify any changed
     * definitions.
     *
     * @throws IOException
     */
    private void mergeConfigs() throws IOException {
        String cfg = Resources.toString(PneumaticCraftRepressurized.class.getResource(HEATBLOCK_CFG_ASSET), Charsets.UTF_8);
        JsonParser parser = new JsonParser();
        JsonObject internalJsonObject = (JsonObject) parser.parse(cfg);

        JsonObject toWrite;
        if (file.exists()) {
            // existing properties file - overwrite defaults, merge in any new block properties
            JsonObject fileJsonObject = (JsonObject) parser.parse(FileUtils.readFileToString(file, Charsets.UTF_8));

            fileJsonObject.add("Description", internalJsonObject.get("Description"));

            JsonObject defaults1 = internalJsonObject.getAsJsonObject("defaults");
            JsonObject defaults2 = fileJsonObject.getAsJsonObject("defaults");
            for (Map.Entry<String, JsonElement> entry : defaults1.entrySet()) {
                if (!defaults2.has(entry.getKey())) {
                    defaults2.add(entry.getKey(), entry.getValue());
                }
            }

            JsonObject blocks1 = internalJsonObject.getAsJsonObject("blocks");
            JsonObject blocks2 = fileJsonObject.getAsJsonObject("blocks");
            for (Map.Entry<String, JsonElement> entry : blocks1.entrySet()) {
                if (!blocks2.has(entry.getKey())) {
                    blocks2.add(entry.getKey(), entry.getValue());
                }
            }

            toWrite = fileJsonObject;
        } else {
            // no existing properties file - just copy the internal one out
            toWrite = internalJsonObject;
        }

        try (PrintWriter out = new PrintWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            out.println(gson.toJson(toWrite));
        }
    }

    @Override
    protected void writeToJson(JsonObject json) {
        // do nothing here; we don't write these values out
    }

    @Override
    public void writeToFile() throws IOException {
        // do nothing here; we don't write these values out
    }

    @Override
    protected void readFromJson(JsonObject json) {
        JsonObject def = json.getAsJsonObject("defaults");
        defaultBlockThermalResistance = def.get("blockThermalResistance").getAsDouble();
        defaultFluidThermalResistance = def.get("fluidThermalResistance").getAsDouble();
        defaultFluidTotalHeat = def.get("fluidTotalHeat").getAsInt();
        ambientTempBiomeModifier = def.get("ambientTemperatureBiomeModifier").getAsDouble();
        ambientTempHeightModifier = def.get("ambientTemperatureHeightModifier").getAsDouble();

        JsonObject sub = json.getAsJsonObject("blocks");
        for (Map.Entry<String, JsonElement> entry : sub.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                JsonObject jsonRecord = entry.getValue().getAsJsonObject();
                try {
                    CustomHeatEntry che = CustomHeatEntry.fromJson(entry.getKey(), jsonRecord);
                    if (che != null) {
                        IBlockState state = che.getBlockState();
                        if (entry.getKey().indexOf('[') == -1) {
                            ignoreVariants.add(state.getBlock());
                        }
                        customHeatEntries.put(makeKeyForState(state), che);
                    } else {
                        String what = entry.getKey().indexOf(':') == -1 ? "fluid" : "block";
                        Log.warning("skipping BlockHeatProperties.cfg entry '" + entry.getKey() + "': unknown " + what + " (mod not loaded?)");
                    }
                } catch (InvalidBlockStateException e) {
                    Log.error("invalid blockstate for " + entry.getKey() + ": " + e.getMessage());
                }
            } else {
                Log.error("Invalid JSON? entry '" + entry.getKey() + "' in " + getConfigFilename());
            }
        }
    }

    // TODO remove in 1.13: we will just store by block then
    private String makeKeyForState(IBlockState blockState) {
        Block b = blockState.getBlock();
        return ignoreVariants.contains(b) ?
                b.getRegistryName().toString() :
                b.getRegistryName() + ":" + b.getMetaFromState(blockState);
    }

    public Map<String, CustomHeatEntry> getCustomHeatEntries() {
        return customHeatEntries;
    }

    public CustomHeatEntry getCustomHeatEntry(IBlockState state) {
        String key = makeKeyForState(state);
        CustomHeatEntry entry = customHeatEntries.get(key);
        if (entry == null) {
            Fluid fluid = FluidRegistry.lookupFluidForBlock(state.getBlock());
            if (fluid != null) {
                entry = buildDefaultFluidEntry(state, fluid);
                customHeatEntries.put(key, entry);
            }
        }
        return entry;
    }

    private CustomHeatEntry buildDefaultFluidEntry(IBlockState state, Fluid fluid) {
        IBlockState transformHot, transformHotFlowing, transformCold, transformColdFlowing;
        if (fluid.getTemperature() >= 1300) {  // lava temperature
            transformHot = null;
            transformHotFlowing = null;
            transformCold = Blocks.OBSIDIAN.getDefaultState();
            transformColdFlowing = Blocks.COBBLESTONE.getDefaultState();
        } else {
            transformHot = Blocks.STONE.getDefaultState();
            transformHotFlowing = Blocks.AIR.getDefaultState();
            transformCold = Blocks.ICE.getDefaultState();
            transformColdFlowing = Blocks.SNOW.getDefaultState();
        }
        return new CustomHeatEntry(
                fluid.getName(), state,
                transformHot, transformHotFlowing,
                transformCold, transformColdFlowing,
                defaultFluidTotalHeat,
                fluid.getTemperature(),
                defaultFluidThermalResistance
        );

    }

    @Override
    public String getConfigFilename() {
        return "BlockHeatProperties";
    }

    public static class CustomHeatEntry {
        private final int totalHeat;
        private final int temperature;
        private final double thermalResistance;
        private final IBlockState blockState;
        private final IBlockState transformHot;
        private final IBlockState transformHotFlowing;
        private final IBlockState transformCold;
        private final IBlockState transformColdFlowing;
        private final String id;
        private final boolean isDefaultState;

        CustomHeatEntry(String id, IBlockState blockState, IBlockState transformHot, IBlockState transformHotFlowing, IBlockState transformCold, IBlockState transformColdFlowing, int totalHeat, int temperature, double thermalResistance) {
            this.totalHeat = totalHeat;
            this.blockState = blockState;
            this.transformHot = transformHot;
            this.transformHotFlowing = transformHotFlowing != null ? transformHotFlowing : transformHot;
            this.transformCold = transformCold;
            this.transformColdFlowing = transformColdFlowing != null ? transformColdFlowing : transformCold;
            this.id = id;
            this.temperature = temperature;
            this.thermalResistance = thermalResistance;
            this.isDefaultState = blockState.getBlock().getDefaultState() == blockState;
        }

        static CustomHeatEntry fromJson(String blockStateId, JsonObject value) throws InvalidBlockStateException {
            IBlockState transformHot = null;
            IBlockState transformHotFlowing = null;
            IBlockState transformCold = null;
            IBlockState transformColdFlowing = null;

            IBlockState blockState = parseBlockState(blockStateId);
            if (blockState == null || blockState.getBlock() == Blocks.AIR) {
                return null;
            }
            Fluid fluid = FluidRegistry.lookupFluidForBlock(blockState.getBlock());

            int totalHeat = 0;
            if (value.has("totalHeat")) {
                totalHeat = value.get("totalHeat").getAsInt();
            } else if (fluid != null) {
                totalHeat = defaultFluidTotalHeat;
            }
            if (totalHeat != 0) {
                transformHot = maybeParseBlockState(value, "transformHot");
                transformHotFlowing = maybeParseBlockState(value, "transformHotFlowing");
                transformCold = maybeParseBlockState(value, "transformCold");
                transformColdFlowing = maybeParseBlockState(value, "transformColdFlowing");
            }

            int temperature;
            if (value.has("temperature")) {
                temperature = value.get("temperature").getAsInt();
            } else {
                if (fluid == null) {
                    throw new JsonSyntaxException(blockState.toString() + ": Non-fluid definitions must have a temperature field!");
                } else {
                    temperature = fluid.getTemperature();
                }
            }

            double thermalResistance;
            if (value.has("thermalResistance")) {
                thermalResistance = value.get("thermalResistance").getAsDouble();
            } else if (fluid == null) {
                thermalResistance = defaultBlockThermalResistance;
            } else {
                thermalResistance = defaultFluidThermalResistance;
            }

            return new CustomHeatEntry(blockStateId,
                    blockState,
                    transformHot,
                    transformHotFlowing,
                    transformCold,
                    transformColdFlowing,
                    totalHeat,
                    temperature,
                    thermalResistance
            );
        }

        public int getTotalHeat() {
            return totalHeat;
        }

        public int getTemperature() {
            return temperature;
        }

        public double getThermalResistance() {
            return thermalResistance;
        }

        public IBlockState getBlockState() {
            return blockState;
        }

        public IBlockState getTransformHot() {
            return transformHot;
        }

        public IBlockState getTransformCold() {
            return transformCold;
        }

        public IBlockState getTransformHotFlowing() {
            return transformHotFlowing;
        }

        public IBlockState getTransformColdFlowing() {
            return transformColdFlowing;
        }

        public String getId() {
            return id;
        }

        public boolean isDefaultState() {
            return isDefaultState;
        }

        private static IBlockState maybeParseBlockState(JsonObject value, String field) throws InvalidBlockStateException {
            return value.has(field) ? parseBlockState(value.get(field).getAsString()) : null;
        }

        private static IBlockState parseBlockState(String s) throws InvalidBlockStateException {
            // special case
            if (s.equals("minecraft:air")) return Blocks.AIR.getDefaultState();

            Fluid fluid = FluidRegistry.getFluid(s);
            if (fluid != null) {
                // not all fluids have an associated block
                return fluid.getBlock() == null ? null : fluid.getBlock().getDefaultState();
            } else if (s.indexOf(':') == -1) {
                Log.warning("BlockHeatProperties.cfg: unknown fluid definition [" + s + "]");
                return null;
            }

            String blockName = s;
            String variant = "";
            int i = s.indexOf('[');
            if (i >= 0) {
                blockName = s.substring(0, i);
                variant = s.substring(i + 1);
                variant = variant.replaceAll("]$", "");
            }
            ResourceLocation rl = new ResourceLocation(blockName);
            Block b = Block.REGISTRY.getObject(rl);
            if (b == Blocks.AIR || b == null) {
                if (Loader.isModLoaded(rl.getNamespace())) {
                    // if mod is loaded, then the config contains an error
                    throw new InvalidBlockStateException("unknown block name: " + rl);
                } else {
                    // mod not loaded - we'll just ignore this entry
                    return null;
                }
            }
            IBlockState state = b.getDefaultState();
            if (variant.isEmpty()) return state;
            BlockStateContainer blockstatecontainer = b.getBlockState();
            for (String v : variant.split(",")) {
                String[] p = v.split("=");
                if (p.length != 2) throw new InvalidBlockStateException("malformed property " + v);
                String propName = p[0];
                String propVal = p[1];
                IProperty<?> property = blockstatecontainer.getProperty(propName);
                if (property == null) throw new InvalidBlockStateException("unknown property " + propName);
                state = setValueHelper(state, property, propVal);
            }
            return state;
        }

        private static <T extends Comparable<T>> IBlockState setValueHelper(IBlockState state, IProperty<T> property, String propVal) {
            Optional<T> optional = property.parseValue(propVal);

            if (optional.isPresent()) {
                return state.withProperty(property, optional.get());
            }  else {
                Log.warning(String.format("Unable to read property: %s with value: %s for blockstate: %s", property, propVal, state));
                return state;
            }
        }
    }
}
