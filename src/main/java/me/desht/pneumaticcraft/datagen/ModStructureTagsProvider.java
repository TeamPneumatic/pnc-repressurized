package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.concurrent.CompletableFuture;

public class ModStructureTagsProvider extends StructureTagsProvider {
    public ModStructureTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Names.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(PneumaticCraftTags.Structures.NO_OIL_LAKES).addTag(StructureTags.VILLAGE);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Structure Tags";
    }
}
