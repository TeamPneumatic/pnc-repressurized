package me.desht.pneumaticcraft.common.config.aux;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.*;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class BlockHeatPropertiesConfig extends AuxConfigJson {
    private static final String HEATBLOCK_CFG_ASSET = "/data/pneumaticcraft/heat/block_heat_properties.json";

    public static final BlockHeatPropertiesConfig INSTANCE = new BlockHeatPropertiesConfig();

    private final Map<ResourceLocation, CustomHeatEntry> customHeatEntries = new HashMap<>();

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
     * @throws IOException if an I/O error occurs
     */
    private void mergeConfigs() throws IOException {
        //noinspection UnstableApiUsage
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
    public void writeToFile() {
        // do nothing here; we don't write these values out
    }

    @Override
    protected void readFromJson(JsonObject json) {
        readSection(json, "blocks");
        readSection(json, "fluids");
    }

    private void readSection(JsonObject json, String section) {
        JsonObject sub = json.getAsJsonObject(section);
        for (Map.Entry<String, JsonElement> entry : sub.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                try {
                    JsonObject jsonRecord = entry.getValue().getAsJsonObject();
                    ResourceLocation rl = new ResourceLocation(entry.getKey());
                    if (ModList.get().isLoaded(rl.getNamespace())) {
                        CustomHeatEntry che = CustomHeatEntry.fromJson(entry.getKey(), jsonRecord);
                        if (che != null) {
                            customHeatEntries.put(che.getBlock().getRegistryName(), che);
                        } else {
                            Log.warning("failed to read " + getConfigFilename() + " " + section + " entry '" + rl);
                        }
                    }
                } catch (Exception e) {
                    Log.error("failed to read " + getConfigFilename() + " entry: " + e.getMessage());
                }
            } else {
                Log.error("Invalid JSON? entry '" + entry.getKey() + "' in " + getConfigFilename());
            }
        }
    }

    public Map<ResourceLocation, CustomHeatEntry> getCustomHeatEntries() {
        return customHeatEntries;
    }

    public CustomHeatEntry getCustomHeatEntry(Block block) {
        ResourceLocation key = block.getRegistryName();
        CustomHeatEntry entry = customHeatEntries.get(key);
        if (entry == null && block instanceof FlowingFluidBlock) {
            Fluid fluid = ((FlowingFluidBlock) block).getFluid();
            if (fluid != null && fluid != Fluids.EMPTY) {
                entry = buildDefaultFluidEntry(block, fluid);
                customHeatEntries.put(key, entry);
            }
        }
        return entry;
    }

    private CustomHeatEntry buildDefaultFluidEntry(Block block, Fluid fluid) {
        Block transformHot, transformHotFlowing, transformCold, transformColdFlowing;
        if (block == null || block == Blocks.AIR) return null;

        if (fluid.getAttributes().getTemperature() >= Fluids.LAVA.getAttributes().getTemperature()) {
            transformHot = null;
            transformHotFlowing = null;
            transformCold = Blocks.OBSIDIAN;
            transformColdFlowing = Blocks.COBBLESTONE;
        } else {
            transformHot = Blocks.STONE;
            transformHotFlowing = Blocks.AIR;
            transformCold = Blocks.ICE;
            transformColdFlowing = Blocks.SNOW;
        }
        return new CustomHeatEntry(block,
                transformHot, transformHotFlowing,
                transformCold, transformColdFlowing,
                PNCConfig.Common.BlockHeatDefaults.defaultFluidTotalHeat,
                fluid.getAttributes().getTemperature(),
                PNCConfig.Common.BlockHeatDefaults.defaultFluidThermalResistance
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
        private final Block block;
        private final Block transformHot;
        private final Block transformHotFlowing;
        private final Block transformCold;
        private final Block transformColdFlowing;
        private final ResourceLocation id;

        CustomHeatEntry(Block block, Block transformHot, Block transformHotFlowing, Block transformCold, Block transformColdFlowing, int totalHeat, int temperature, double thermalResistance) {
            this.id = block.getRegistryName();
            this.totalHeat = totalHeat;
            this.block = block;
            this.transformHot = transformHot;
            this.transformHotFlowing = transformHotFlowing != null ? transformHotFlowing : transformHot;
            this.transformCold = transformCold;
            this.transformColdFlowing = transformColdFlowing != null ? transformColdFlowing : transformCold;
            this.temperature = temperature;
            this.thermalResistance = thermalResistance;
        }

        static CustomHeatEntry fromJson(String registryName, JsonObject value) {
            Block transformHot = null;
            Block transformHotFlowing = null;
            Block transformCold = null;
            Block transformColdFlowing = null;

            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName));
            if (block == null || block == Blocks.AIR) {
                return null;
            }
            IFluidState fluidstate = block.getDefaultState().getFluidState();
            Fluid fluid = fluidstate.getFluid();

            int totalHeat = 0;
            if (value.has("totalHeatCapacity")) {
                totalHeat = value.get("totalHeatCapacity").getAsInt();
            } else if (fluid != Fluids.EMPTY) {
                totalHeat = PNCConfig.Common.BlockHeatDefaults.defaultFluidTotalHeat;
            }
            if (totalHeat != 0) {
                transformHot = maybeGetBlock(block, value, "transformHot");
                transformHotFlowing = maybeGetBlock(block, value, "transformHotFlowing");
                transformCold = maybeGetBlock(block, value, "transformCold");
                transformColdFlowing = maybeGetBlock(block, value, "transformColdFlowing");
            }

            int temperature;
            if (value.has("temperature")) {
                temperature = value.get("temperature").getAsInt();
            } else {
                if (fluid == Fluids.EMPTY) {
                    throw new JsonSyntaxException(block.toString() + ": Non-fluid definitions must have a temperature field!");
                } else {
                    temperature = fluid.getAttributes().getTemperature();
                }
            }

            double thermalResistance;
            if (value.has("thermalResistance")) {
                thermalResistance = value.get("thermalResistance").getAsDouble();
            } else if (fluid == null) {
                thermalResistance = PNCConfig.Common.BlockHeatDefaults.defaultBlockThermalResistance;
            } else {
                thermalResistance = PNCConfig.Common.BlockHeatDefaults.defaultFluidThermalResistance;
            }

            return new CustomHeatEntry(block,
                    transformHot,
                    transformHotFlowing,
                    transformCold,
                    transformColdFlowing,
                    totalHeat,
                    temperature,
                    thermalResistance
            );
        }

        private static Block maybeGetBlock(Block b, JsonObject value, String field) {
            if (!value.has(field)) return null;
            ResourceLocation rl = new ResourceLocation(value.get(field).getAsString());

            if (ForgeRegistries.BLOCKS.containsKey(rl)) {
                return ForgeRegistries.BLOCKS.getValue(rl);
            } else if (ForgeRegistries.FLUIDS.containsKey(rl)) {
                return ForgeRegistries.FLUIDS.getValue(rl).getDefaultState().getBlockState().getBlock();
            } else {
                throw new JsonSyntaxException(String.format("bad value '%s' for field '%s' in block '%s'", rl, field, b.getRegistryName()));
            }
        }

        public int getTotalHeatCapacity() {
            return totalHeat;
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

        public Block getTransformHot() {
            return transformHot;
        }

        public Block getTransformCold() {
            return transformCold;
        }

        public Block getTransformHotFlowing() {
            return transformHotFlowing;
        }

        public Block getTransformColdFlowing() {
            return transformColdFlowing;
        }

        public ResourceLocation getId() {
            return id;
        }

    }
}
