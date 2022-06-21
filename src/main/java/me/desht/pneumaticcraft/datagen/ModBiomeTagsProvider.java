package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        appendToTag(PneumaticCraftTags.Biomes.OIL_LAKES_SURFACE, BiomeTags.IS_OVERWORLD);
        appendToTag(PneumaticCraftTags.Biomes.OIL_LAKES_UNDERGROUND, BiomeTags.IS_OVERWORLD);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Biome Tags";
    }

    @SafeVarargs
    private <T> T[] resolveAll(IntFunction<T[]> creator, Supplier<? extends T>... suppliers) {
        return Arrays.stream(suppliers).map(Supplier::get).toArray(creator);
    }

    @SafeVarargs
    private void createTag(TagKey<Biome> tag, Supplier<? extends Biome>... blocks) {
        tag(tag).add(resolveAll(Biome[]::new, blocks));
    }

    @SafeVarargs
    private void appendToTag(TagKey<Biome> tag, TagKey<Biome>... toAppend) {
        tag(tag).addTags(toAppend);
    }

    @SafeVarargs
    private void createAndAppend(TagKey<Biome> tag, TagKey<Biome> to, Supplier<? extends Biome>... biomes) {
        createTag(tag, biomes);
        appendToTag(to, tag);
    }
}
