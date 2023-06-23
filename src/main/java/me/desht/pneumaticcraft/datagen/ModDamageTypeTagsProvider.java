package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.DamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagsProvider extends TagsProvider<DamageType> {
    protected ModDamageTypeTagsProvider( PackOutput output,
                                         CompletableFuture<HolderLookup.Provider> provider,
                                         @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, provider, Names.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(DamageTypes.SECURITY_STATION)
                .add(DamageTypes.MINIGUN_AP)
                .add(DamageTypes.PLASTIC_BLOCK);

        tag(PneumaticCraftTags.DamageTypes.MINIGUN)
                .add(DamageTypes.MINIGUN)
                .add(DamageTypes.MINIGUN_AP);

        tag(PneumaticCraftTags.DamageTypes.ACID).add(DamageTypes.ETCHING_ACID);
        tag(PneumaticCraftTags.DamageTypes.SECURITY_STATION).add(DamageTypes.SECURITY_STATION);
        tag(PneumaticCraftTags.DamageTypes.PLASTIC_BLOCK).add(DamageTypes.PLASTIC_BLOCK);
        tag(PneumaticCraftTags.DamageTypes.PRESSURE).add(DamageTypes.PRESSURE);
    }
}
