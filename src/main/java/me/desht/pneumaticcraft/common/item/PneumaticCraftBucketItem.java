package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class PneumaticCraftBucketItem extends BucketItem implements IFluidCapProvider {
    public PneumaticCraftBucketItem(Fluid fluid) {
        super(fluid, ModItems.filledBucketProps());
    }

    @Override
    public ResourceHandler<FluidResource> provideFluidCapability(ItemAccess itemAccess) {
        return new BucketResourceHandler(itemAccess);
    }
}
