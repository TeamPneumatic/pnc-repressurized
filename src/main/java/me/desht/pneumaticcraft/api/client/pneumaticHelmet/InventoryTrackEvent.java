package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Fired when a helmet Block Tracker is about to track an inventory. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 *
 * The tile inventory is known to support CapabilityItemHandler.ITEM_HANDLER_CAPABILITY on (at least)
 * the "null" face when the event is fired.
 *
 * @author MineMaarten
 */
@Cancelable
public class InventoryTrackEvent extends Event {
    private final TileEntity te;

    public InventoryTrackEvent(TileEntity te) {
        this.te = te;
    }

    public TileEntity getTileEntity() {
        return te;
    }

    public IItemHandler getInventory() {
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }
}
