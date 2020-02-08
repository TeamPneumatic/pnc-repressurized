package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.core.ModFluids;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.Tag;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerTags() {
        createTag(PneumaticCraftTags.Fluids.OIL, ModFluids.OIL);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Fluid Tags";
    }

    @SafeVarargs
    private final <T> T[] resolveAll(IntFunction<T[]> creator, Supplier<? extends T>... suppliers) {
        return Arrays.stream(suppliers).map(Supplier::get).toArray(creator);
    }

    @SafeVarargs
    private final void createTag(Tag<Fluid> tag, Supplier<? extends Fluid>... blocks) {
        getBuilder(tag).add(resolveAll(Fluid[]::new, blocks));
    }

    @SafeVarargs
    private final void appendToTag(Tag<Fluid> tag, Tag<Fluid>... toAppend) {
        getBuilder(tag).add(toAppend);
    }

    @SafeVarargs
    private final void createAndAppend(Tag<Fluid> tag, Tag<Fluid> to, Supplier<? extends Fluid>... blocks) {
        createTag(tag, blocks);
        appendToTag(to, tag);
    }
}
