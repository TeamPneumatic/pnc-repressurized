package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

// doesn't work right now, BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE doesn't appear to be present
@SuppressWarnings("unused")
public class ModConfiguredStructureFeatureTagsProvider extends TagsProvider<Structure> {
    public ModConfiguredStructureFeatureTagsProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, BuiltinRegistries.STRUCTURES, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(PneumaticCraftTags.ConfiguredStructures.NO_OIL_LAKES);
    }

    @Override
    public String getName() {
        return "Configured Structure Tags Provider";
    }
}
