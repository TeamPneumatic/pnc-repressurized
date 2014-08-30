package pneumaticCraft.client.gui;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class GuiSecurityStationBase extends GuiPneumaticContainerBase{

    public GuiSecurityStationBase(Container par1Container){
        super(par1Container);
    }

    /**
     * Returns the slot at the given coordinates or null if there is none.
     */
    public Slot getSlotAtPosition(int par1, int par2){
        for(int k = 0; k < inventorySlots.inventorySlots.size(); ++k) {
            Slot slot = (Slot)inventorySlots.inventorySlots.get(k);

            if(isMouseOverSlot(slot, par1, par2)) {
                return slot;
            }
        }

        return null;
    }

    /**
     * Returns if the passed mouse position is over the specified slot.
     */
    protected boolean isMouseOverSlot(Slot par1Slot, int par2, int par3){
        return func_146978_c(par1Slot.xDisplayPosition, par1Slot.yDisplayPosition, 16, 16, par2, par3);
    }
}
