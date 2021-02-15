package me.desht.pneumaticcraft.common.villages;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraft.village.PointOfInterestType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * This gets around the fact that sound events might not be registered by the time the villager profession is.
 */
public class ModVillagerProfession extends VillagerProfession {
    private final List<Supplier<SoundEvent>> soundEventSuppliers;

    @SafeVarargs
    public ModVillagerProfession(String nameIn, PointOfInterestType pointOfInterestIn, ImmutableSet<Item> specificItemsIn, ImmutableSet<Block> relatedWorldBlocksIn, Supplier<SoundEvent>... soundEventSuppliers) {
        super(nameIn, pointOfInterestIn, specificItemsIn, relatedWorldBlocksIn, null);
        this.soundEventSuppliers = Arrays.asList(soundEventSuppliers);
    }

    @Nullable
    @Override
    public SoundEvent getSound() {
        int n = ThreadLocalRandom.current().nextInt(soundEventSuppliers.size());
        return soundEventSuppliers.get(n).get();
    }
}
