package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModGameEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.tags.GameEventTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModGameEventTagsProvider extends GameEventTagsProvider {
    public ModGameEventTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PneumaticCraftTags.GameEvents.JET_BOOTS)
                .add(ModGameEvents.JET_BOOTS_FLY.getKey())
                .add(ModGameEvents.JET_BOOTS_FLY_BUILDER.getKey());

        tag(GameEventTags.VIBRATIONS).addTag(PneumaticCraftTags.GameEvents.JET_BOOTS);
        tag(GameEventTags.WARDEN_CAN_LISTEN).addTag(PneumaticCraftTags.GameEvents.JET_BOOTS);
    }
}
