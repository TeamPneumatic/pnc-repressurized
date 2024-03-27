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

package me.desht.pneumaticcraft.datagen.recipe;

import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;

/**
 * Currently unused.  Would require a lot of extra mods in the dev workspace to use properly.  Maybe this should
 * just take string block and blockstate names?  For now though the heat properties files remain as non-generated
 * JSONs while I decide how best to handle it.
 */
@SuppressWarnings("unused")
public class HeatPropertiesRecipeBuilder extends AbstractPNCRecipeBuilder {
    private final Block block;
    private final int temperature;
    private final double thermalResistance;
    private final Map<String, String> predicates;
    private final int heatCapacity;
    private final BlockState transformHot;
    private final BlockState transformCold;
    private final BlockState transformHotFlowing;
    private final BlockState transformColdFlowing;

    public HeatPropertiesRecipeBuilder(Block block, int temperature, double thermalResistance, Map<String,String> predicates) {
        this(block, temperature, thermalResistance, predicates, 0, null, null, null, null);
    }

    public HeatPropertiesRecipeBuilder(Block block, int temperature, double thermalResistance, Map<String,String> predicates, int heatCapacity,
                                       BlockState transformHot, BlockState transformCold, BlockState transformHotFlowing, BlockState transformColdFlowing) {
        this.block = block;
        this.temperature = temperature;
        this.thermalResistance = thermalResistance;
        this.predicates = predicates;
        this.heatCapacity = heatCapacity;
        this.transformHot = transformHot;
        this.transformCold = transformCold;
        this.transformHotFlowing = transformHotFlowing;
        this.transformColdFlowing = transformColdFlowing;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new HeatPropertiesRecipeImpl(block,
                Optional.ofNullable(transformHot), Optional.ofNullable(transformHotFlowing),
                Optional.ofNullable(transformCold), Optional.ofNullable(transformColdFlowing),
                Optional.of(heatCapacity), temperature, Optional.of(thermalResistance), predicates, ""), null);
    }
}
