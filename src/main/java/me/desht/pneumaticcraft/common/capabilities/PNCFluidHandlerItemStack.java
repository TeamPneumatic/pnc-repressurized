package me.desht.pneumaticcraft.common.capabilities;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Item fluid handler which stores its fluid in a data component, and only accepts certain fluids.
 */
public class PNCFluidHandlerItemStack extends ItemAccessFluidHandler {
    private final Predicate<Fluid> fluidPredicate;

    /**
     * @param componentType the data component the fluid is stored in
     * @param itemAccess    access to the container item
     * @param capacity      the maximum capacity of this fluid tank
     * @param fluidPredicate the fluids which may be stored
     */
    public PNCFluidHandlerItemStack(Supplier<DataComponentType<SimpleFluidContent>> componentType, ItemAccess itemAccess, int capacity, Predicate<Fluid> fluidPredicate) {
        super(itemAccess, componentType.get(), capacity);
        this.fluidPredicate = fluidPredicate;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return super.isValid(index, resource) && (resource.isEmpty() || fluidPredicate.test(resource.getFluid()));
    }
}
