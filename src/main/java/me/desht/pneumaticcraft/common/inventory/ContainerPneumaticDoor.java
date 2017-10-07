package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerPneumaticDoor extends ContainerPneumaticBase<TileEntityPneumaticDoorBase> {

    public ContainerPneumaticDoor(InventoryPlayer inventoryPlayer, TileEntityPneumaticDoorBase te) {
        super(te);

        addUpgradeSlots(23, 29);

        // Add the camo slot.
        addSlotToContainer(new SlotCamouflage(te, TileEntityPneumaticDoorBase.CAMO_SLOT, 77, 36));

        addPlayerSlots(inventoryPlayer, 84);
    }
}
