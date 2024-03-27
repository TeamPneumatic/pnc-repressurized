package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.function.Supplier;

public class PneumaticCraftBucketItem extends BucketItem implements IFluidCapProvider {
    public PneumaticCraftBucketItem(Supplier<? extends Fluid> supplier) {
        super(supplier, ModItems.filledBucketProps());
    }

    @Override
    public IFluidHandlerItem provideFluidCapability(ItemStack stack) {
        return new FluidBucketWrapper(stack);
    }

//    @Override
//    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
//        return new FluidBucketWrapper(stack);
//    }
}
