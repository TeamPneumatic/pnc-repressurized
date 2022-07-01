package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.core.ModVillagers;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class ModPoiTypeTagsProvider extends PoiTypeTagsProvider {
    public ModPoiTypeTagsProvider(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        var appender = tag(PoiTypeTags.ACQUIRABLE_JOB_SITE);
        ModVillagers.POI.getEntries().stream().map(RegistryObject::getKey).forEach(appender::add);
    }

    @Override
    public String getName() {
        return "PneumaticCraft POI Type Tags";
    }
}
