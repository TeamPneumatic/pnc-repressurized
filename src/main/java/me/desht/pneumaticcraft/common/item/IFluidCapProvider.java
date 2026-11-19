package me.desht.pneumaticcraft.common.item;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public interface IFluidCapProvider {
    ResourceHandler<FluidResource> provideFluidCapability(ItemAccess itemAccess);
}
