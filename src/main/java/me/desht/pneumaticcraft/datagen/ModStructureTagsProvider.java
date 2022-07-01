package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModStructureTagsProvider extends TagsProvider<Structure> {
    public ModStructureTagsProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, BuiltinRegistries.STRUCTURES, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(PneumaticCraftTags.Structures.NO_OIL_LAKES).addTags(StructureTags.VILLAGE);
    }

    @Override
    public String getName() {
        return "PneumaticCraft Structure Tags";
    }
}
