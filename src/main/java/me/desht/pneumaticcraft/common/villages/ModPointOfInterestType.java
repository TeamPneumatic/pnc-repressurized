package me.desht.pneumaticcraft.common.villages;

import net.minecraft.block.BlockState;
import net.minecraft.util.SoundEvent;
import net.minecraft.village.PointOfInterestType;

import java.util.Set;
import java.util.function.Supplier;

public class ModPointOfInterestType extends PointOfInterestType {
    private final Supplier<SoundEvent> soundSupplier;

    public ModPointOfInterestType(String name, Set<BlockState> blockstates, int tickets, Supplier<SoundEvent> soundSupplier, int range) {
        super(name, blockstates, tickets, null, range);
        this.soundSupplier = soundSupplier;
    }

//    @Nullable
//    @Override
//    public SoundEvent getWorkSound() {
//        return soundSupplier.get();
//    }
}
