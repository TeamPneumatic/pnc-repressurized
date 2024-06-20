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
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.*;

public enum BlockHeatProperties implements Iterable<HeatPropertiesRecipe> {
    INSTANCE;

    private final ArrayListMultimap<Block, HeatPropertiesRecipe> customHeatEntries = ArrayListMultimap.create();

    public static BlockHeatProperties getInstance() {
        return INSTANCE;
    }

    public HeatPropertiesRecipe getCustomHeatEntry(Level world, BlockState state) {
        if (customHeatEntries.isEmpty()) {
            populateCustomHeatEntries(world);
        }
        return customHeatEntries.get(state.getBlock()).stream()
                .filter(entry -> entry.matchState(state))
                .findFirst()
                .orElse(null);
    }

    public Collection<HeatPropertiesRecipe> getAllEntries(Level world) {
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

    private void populateCustomHeatEntries(Level world) {
        ModRecipeTypes.getRecipes(world, ModRecipeTypes.BLOCK_HEAT_PROPERTIES)
                        .forEach(recipe -> customHeatEntries.put(recipe.value().getBlock(), recipe.value()));

        registerDefaultFluidValues();
    }

    private void registerDefaultFluidValues() {
        // add defaulted values for all fluids which don't already have a custom entry
        for (ResourceLocation fluidId : BuiltInRegistries.FLUID.keySet()) {
            Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
            if (fluid == Fluids.EMPTY) {
                continue;
            } else if (!ConfigHelper.common().heat.addDefaultFluidEntries.get() && !fluidId.getNamespace().equals("minecraft")) {
                continue;
            }
            Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
            if (!(block instanceof LiquidBlock) || customHeatEntries.containsKey(block)) {
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
        int temperature = fluid.getFluidType().getTemperature();
        if (temperature >= Fluids.LAVA.getFluidType().getTemperature()) {
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
                block,
                new HeatPropertiesRecipe.Transforms(
                        Optional.ofNullable(transformHot), Optional.of(transformCold),
                        Optional.ofNullable(transformHotFlowing), Optional.of(transformColdFlowing)
                ),
                Optional.of(ConfigHelper.common().heat.defaultFluidHeatCapacity.get()),
                temperature,
                Optional.of(ConfigHelper.common().heat.fluidThermalResistance.get()),
                Collections.emptyMap(),
                ""
        );
    }

    @Override
    public Iterator<HeatPropertiesRecipe> iterator() {
        return customHeatEntries.values().iterator();
    }
}
