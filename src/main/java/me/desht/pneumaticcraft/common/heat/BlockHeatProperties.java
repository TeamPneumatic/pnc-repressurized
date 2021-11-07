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

package me.desht.pneumaticcraft.common.heat;

import com.google.common.collect.ArrayListMultimap;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.heat.HeatRegistrationEvent;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
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
            if (fluid == Fluids.EMPTY) {
                continue;
            } else if (!PNCConfig.Common.Heat.addDefaultFluidEntries && !fluid.getRegistryName().getNamespace().equals("minecraft")) {
                continue;
            }
            Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
            if (!(block instanceof FlowingFluidBlock) || customHeatEntries.containsKey(block)) {
                // block must be a fluid block and not already have a custom heat entry
                continue;
            }
            List<HeatPropertiesRecipe> entry = customHeatEntries.get(block);
            if (entry.isEmpty()) {
                customHeatEntries.put(block, buildDefaultFluidEntry(block, fluid));
            }
        }
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
            transformCold = Blocks.OBSIDIAN.defaultBlockState();
            transformColdFlowing = Blocks.COBBLESTONE.defaultBlockState();
        } else if (temperature <= 273) {
            transformHot = Blocks.SNOW_BLOCK.defaultBlockState();
            transformHotFlowing = Blocks.SNOW.defaultBlockState();
            transformCold = Blocks.BLUE_ICE.defaultBlockState();
            transformColdFlowing = Blocks.SNOW.defaultBlockState();
        } else {
            transformHot = Blocks.AIR.defaultBlockState();
            transformHotFlowing = Blocks.AIR.defaultBlockState();
            transformCold = Blocks.ICE.defaultBlockState();
            transformColdFlowing = Blocks.SNOW.defaultBlockState();
        }
        return new HeatPropertiesRecipeImpl(
                block.getRegistryName(),
                block,
                transformHot, transformHotFlowing,
                transformCold, transformColdFlowing,
                PNCConfig.Common.Heat.defaultFluidHeatCapacity,
                temperature,
                PNCConfig.Common.Heat.defaultFluidThermalResistance,
                Collections.emptyMap(),
                ""
        );
    }

    @Override
    public Iterator<HeatPropertiesRecipe> iterator() {
        return customHeatEntries.values().iterator();
    }
}
