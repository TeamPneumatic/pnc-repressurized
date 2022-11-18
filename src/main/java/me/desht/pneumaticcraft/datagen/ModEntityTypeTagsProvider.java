package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(PneumaticCraftTags.EntityTypes.VACUUM_TRAP_BLACKLISTED);
        tag(PneumaticCraftTags.EntityTypes.VACUUM_TRAP_WHITELISTED);
        tag(PneumaticCraftTags.EntityTypes.OMNIHOPPER_BLACKLISTED).add(EntityType.VILLAGER);
    }
}
