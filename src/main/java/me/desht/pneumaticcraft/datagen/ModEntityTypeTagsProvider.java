package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Names.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(PneumaticCraftTags.EntityTypes.VACUUM_TRAP_BLACKLISTED).addTag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED);
        tag(PneumaticCraftTags.EntityTypes.VACUUM_TRAP_WHITELISTED);
        tag(PneumaticCraftTags.EntityTypes.OMNIHOPPER_BLACKLISTED).add(EntityType.VILLAGER);

        tag(PneumaticCraftTags.EntityTypes.BASIC_DRONES).add(ModEntityTypes.COLLECTOR_DRONE.get());
        tag(PneumaticCraftTags.EntityTypes.BASIC_DRONES).add(ModEntityTypes.GUARD_DRONE.get());
        tag(PneumaticCraftTags.EntityTypes.BASIC_DRONES).add(ModEntityTypes.HARVESTING_DRONE.get());
        tag(PneumaticCraftTags.EntityTypes.BASIC_DRONES).add(ModEntityTypes.LOGISTICS_DRONE.get());

        tag(PneumaticCraftTags.EntityTypes.DRONES).add(ModEntityTypes.DRONE.get());
        tag(PneumaticCraftTags.EntityTypes.DRONES).addTag(PneumaticCraftTags.EntityTypes.BASIC_DRONES);

        var radImmune = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", "radiation_immune"));
        tag(radImmune).addTag(PneumaticCraftTags.EntityTypes.DRONES);

        // no PNC entities are suitable for picking up with Carry On
        for (var entityType : ModEntityTypes.ENTITY_TYPES.getEntries()) {
            tag(PneumaticCraftTags.EntityTypes.CARRYON_BLACKLISTED).add(entityType.get());
        }
    }
}
