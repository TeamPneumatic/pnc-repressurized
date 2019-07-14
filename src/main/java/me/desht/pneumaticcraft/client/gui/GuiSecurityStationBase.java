package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiSecurityStationBase<C extends ContainerPneumaticBase<TileEntitySecurityStation>> extends GuiPneumaticContainerBase<C,TileEntitySecurityStation> {

    GuiSecurityStationBase(C container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    /**
     * Returns the slot at the given coordinates or null if there is none.
     */
    public Slot getSlotAtPosition(int x, int y) {
        for (int k = 0; k < container.inventorySlots.size(); ++k) {
            Slot slot = container.inventorySlots.get(k);

            if (isMouseOverSlot(slot, x, y)) {
                return slot;
            }
        }

        return null;
    }

    private boolean isMouseOverSlot(Slot slot, int x, int y) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, x, y);
    }
}
