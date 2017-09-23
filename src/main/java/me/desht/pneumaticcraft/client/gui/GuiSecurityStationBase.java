package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class GuiSecurityStationBase extends GuiPneumaticContainerBase<TileEntitySecurityStation> {

    public GuiSecurityStationBase(Container par1Container, TileEntitySecurityStation te, String guiTexture) {
        super(par1Container, te, guiTexture);
    }

    /**
     * Returns the slot at the given coordinates or null if there is none.
     */
    public Slot getSlotAtPosition(int par1, int par2) {
        for (int k = 0; k < inventorySlots.inventorySlots.size(); ++k) {
            Slot slot = inventorySlots.inventorySlots.get(k);

            if (isMouseOverSlot(slot, par1, par2)) {
                return slot;
            }
        }

        return null;
    }

    /**
     * Returns if the passed mouse position is over the specified slot.
     */
    protected boolean isMouseOverSlot(Slot par1Slot, int par2, int par3) {
        return isPointInRegion(par1Slot.xPos, par1Slot.yPos, 16, 16, par2, par3);
    }
}
