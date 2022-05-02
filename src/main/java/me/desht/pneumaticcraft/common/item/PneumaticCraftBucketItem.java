package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PneumaticCraftBucketItem extends BucketItem {
    public PneumaticCraftBucketItem(Supplier<? extends Fluid> supplier) {
        super(supplier, ModItems.filledBucketProps());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack);
    }
}
