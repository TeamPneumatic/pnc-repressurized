package me.desht.pneumaticcraft.common.heat;

import com.google.common.collect.ArrayListMultimap;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.heat.HeatRegistrationEvent;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public enum BlockHeatProperties implements Iterable<HeatPropertiesRecipe> {
    INSTANCE;

    private final ArrayListMultimap<Block, HeatPropertiesRecipe> customHeatEntries = ArrayListMultimap.create();

    public static BlockHeatProperties getInstance() {
        return INSTANCE;
    }

    public HeatPropertiesRecipe getCustomHeatEntry(World world, BlockState state) {
        if (customHeatEntries.isEmpty()) {
            populateCustomHeatEntries(world);
        }
        return customHeatEntries.get(state.getBlock()).stream()
                .filter(entry -> entry.matchState(state))
                .findFirst()
                .orElse(null);
    }

    public Collection<HeatPropertiesRecipe> getAllEntries(World world) {
        if (customHeatEntries.isEmpty()) {
            populateCustomHeatEntries(world);
        }
        return customHeatEntries.values();
    }

    public void clear() {
        customHeatEntries.clear();
    }

    public void register(Block block, HeatPropertiesRecipe entry) {
        customHeatEntries.put(block, entry);
    }

    private void populateCustomHeatEntries(World world) {
        PneumaticCraftRecipeType.HEAT_PROPERTIES.getRecipes(world)
                .forEach((key, recipe) -> customHeatEntries.put(recipe.getBlock(), recipe));

        // give other mods a chance to programmatically add simple heat properties (no transitions, just temperature & resistance)
        MinecraftForge.EVENT_BUS.post(new HeatRegistrationEvent(HeatExchangerManager.getInstance()));

        registerDefaultFluidValues();
    }

    private void registerDefaultFluidValues() {
        // add defaulted values for all fluids which don't already have a custom entry
        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            if (fluid == Fluids.EMPTY) continue;
            BlockState state = fluid.getDefaultState().getBlockState();
            // block must be a fluid block and not already have a custom heat entry
            if (!(state.getBlock() instanceof FlowingFluidBlock) || customHeatEntries.containsKey(state.getBlock())) {
                continue;
            }
            if (!BlockHeatProperties.getInstance().getOrCreateCustomFluidEntry(fluid)) {
                Log.warning("unable to build custom heat entry for fluid %s (block %s) ",
                        fluid.getRegistryName(), state.getBlock().getRegistryName());
            }
        }
    }

    /**
     * Get or create a custom heat entry for the given fluid.  If a matching fluid entry already exists, return it.
     * Otherwise, build a defaulted fluid entry based on the fluid's temperature and default thermal properties and
     * add it to the custom entries.
     *
     * @param fluid a fluid
     * @return the custom heat entry, or null if the fluid doesn't have a block
     */
    private boolean getOrCreateCustomFluidEntry(Fluid fluid) {
        BlockState state = fluid.getDefaultState().getBlockState();
        if (!(state.getBlock() instanceof FlowingFluidBlock)) return false;

        List<HeatPropertiesRecipe> entry = customHeatEntries.get(state.getBlock());
        if (entry.isEmpty()) {
            customHeatEntries.put(state.getBlock(), buildDefaultFluidEntry(state.getBlock(), fluid));
        }
        return true;
    }

    /**
     * For fluids which do have blocks, but don't have a custom heat entry defined, set up a default entry.
     * @param block the fluid block
     * @param fluid the fluid
     * @return a new custom heat entry for this fluid
     */
    private HeatPropertiesRecipe buildDefaultFluidEntry(Block block, Fluid fluid) {
        BlockState transformHot, transformHotFlowing, transformCold, transformColdFlowing;
        int temperature = fluid.getAttributes().getTemperature();
        if (temperature >= Fluids.LAVA.getAttributes().getTemperature()) {
            transformHot = null;
            transformHotFlowing = null;
            transformCold = Blocks.OBSIDIAN.getDefaultState();
            transformColdFlowing = Blocks.COBBLESTONE.getDefaultState();
        } else if (temperature <= 273) {
            transformHot = Blocks.SNOW_BLOCK.getDefaultState();
            transformHotFlowing = Blocks.SNOW.getDefaultState();
            transformCold = Blocks.BLUE_ICE.getDefaultState();
            transformColdFlowing = Blocks.SNOW.getDefaultState();
        } else {
            transformHot = Blocks.AIR.getDefaultState();
            transformHotFlowing = Blocks.AIR.getDefaultState();
            transformCold = Blocks.ICE.getDefaultState();
            transformColdFlowing = Blocks.SNOW.getDefaultState();
        }
        return new HeatPropertiesRecipeImpl(
                block.getRegistryName(),
                block,
                transformHot, transformHotFlowing,
                transformCold, transformColdFlowing,
                PNCConfig.Common.Heat.defaultFluidHeatCapacity,
                temperature,
                PNCConfig.Common.Heat.defaultFluidThermalResistance,
                Collections.emptyMap()
        );
    }

    @Override
    public Iterator<HeatPropertiesRecipe> iterator() {
        return customHeatEntries.values().iterator();
    }
}
