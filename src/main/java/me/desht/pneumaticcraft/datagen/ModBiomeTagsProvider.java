package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        appendToTag(PneumaticCraftTags.Biomes.OIL_LAKES_SURFACE, BiomeTags.IS_OVERWORLD);
        appendToTag(PneumaticCraftTags.Biomes.OIL_LAKES_UNDERGROUND, BiomeTags.IS_OVERWORLD);
    }

    @SafeVarargs
    private void appendToTag(TagKey<Biome> tag, TagKey<Biome>... toAppend) {
        tag(tag).addTags(toAppend);
    }
}
