package me.desht.pneumaticcraft.api.drone;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fluids.FluidStack;

/**
 * Fired (on the MinecraftForge.EVENT_BUS) when
 * {@link IDroneRegistry#retrieveItemsAmazonStyle(GlobalPos, ItemStack...)} or
 * {@link IDroneRegistry#retrieveFluidAmazonStyle(GlobalPos, FluidStack)}
 * has successfully retrieved the items requested.  The drone passed to this event is the same as the one returned by
 * the retrieval method.
 */
public class AmadronRetrievalEvent extends Event {
    public final IDrone drone;

    public AmadronRetrievalEvent(IDrone drone) {
        this.drone = drone;
    }
}
