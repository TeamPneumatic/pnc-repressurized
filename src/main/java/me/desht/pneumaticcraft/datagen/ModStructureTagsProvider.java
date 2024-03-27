package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModStructureTagsProvider extends TagsProvider<Structure> {
    public ModStructureTagsProvider(DataGenerator pGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator.getPackOutput(), Registries.STRUCTURE, lookupProvider, Names.MOD_ID, existingFileHelper);
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
