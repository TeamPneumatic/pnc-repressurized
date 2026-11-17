package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Names.MOD_ID);
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
