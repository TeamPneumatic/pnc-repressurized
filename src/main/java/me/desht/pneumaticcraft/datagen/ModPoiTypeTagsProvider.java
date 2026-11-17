package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModVillagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.concurrent.CompletableFuture;

public class ModPoiTypeTagsProvider extends PoiTypeTagsProvider {
    public ModPoiTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Names.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        var appender = tag(PoiTypeTags.ACQUIRABLE_JOB_SITE);
        ModVillagers.POI.getEntries().stream().map(DeferredHolder::getKey).forEach(appender::add);
    }

    @Override
    public String getName() {
        return "PneumaticCraft POI Type Tags";
    }
}
