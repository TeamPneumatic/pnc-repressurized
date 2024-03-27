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

package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(generatorIn.getPackOutput(), lookupProvider, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        createTag(PneumaticCraftTags.Fluids.CRUDE_OIL, ModFluids.OIL);
        createTag(PneumaticCraftTags.Fluids.ETCHING_ACID, ModFluids.ETCHING_ACID);
        createTag(PneumaticCraftTags.Fluids.PLASTIC, ModFluids.PLASTIC);
        createTag(PneumaticCraftTags.Fluids.LUBRICANT, ModFluids.LUBRICANT);
        createTag(PneumaticCraftTags.Fluids.DIESEL, ModFluids.DIESEL);
        createTag(PneumaticCraftTags.Fluids.KEROSENE, ModFluids.KEROSENE);
        createTag(PneumaticCraftTags.Fluids.GASOLINE, ModFluids.GASOLINE);
        createTag(PneumaticCraftTags.Fluids.LPG, ModFluids.LPG);
        createTag(PneumaticCraftTags.Fluids.YEAST_CULTURE, ModFluids.YEAST_CULTURE);
        createTag(PneumaticCraftTags.Fluids.ETHANOL, ModFluids.ETHANOL);
        createTag(PneumaticCraftTags.Fluids.PLANT_OIL, ModFluids.VEGETABLE_OIL);
        createTag(PneumaticCraftTags.Fluids.BIODIESEL, ModFluids.BIODIESEL);
        createTag(PneumaticCraftTags.Fluids.EXPERIENCE, ModFluids.MEMORY_ESSENCE);

        createAndAppend(PneumaticCraftTags.Fluids.CRUDE_OIL, PneumaticCraftTags.Fluids.SEISMIC);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Fluid Tags";
    }

    @SafeVarargs
    private <T> T[] resolveAll(IntFunction<T[]> creator, Supplier<? extends T>... suppliers) {
        return Arrays.stream(suppliers).map(Supplier::get).toArray(creator);
    }

    @SafeVarargs
    private void createTag(TagKey<Fluid> tag, Supplier<? extends Fluid>... blocks) {
        tag(tag).add(resolveAll(Fluid[]::new, blocks));
    }

    @SafeVarargs
    private void appendToTag(TagKey<Fluid> tag, TagKey<Fluid>... toAppend) {
        tag(tag).addTags(toAppend);
    }

    @SafeVarargs
    private void createAndAppend(TagKey<Fluid> tag, TagKey<Fluid> to, Supplier<? extends Fluid>... fluids) {
        createTag(tag, fluids);
        appendToTag(to, tag);
    }
}
