package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerPressureChamberInterface extends ContainerPneumaticBase<TileEntityPressureChamberInterface> {

    public ContainerPressureChamberInterface(InventoryPlayer inventoryPlayer, TileEntityPressureChamberInterface te) {
        super(te);

        // add the transfer slot
        addSlotToContainer(new SlotUntouchable(te.getPrimaryInventory(), 0, 66, 35));

        addUpgradeSlots(20, 26);

        addPlayerSlots(inventoryPlayer, 84);

        // add the export filter slots
        //  - after the player slots so they won't be shift-clicked.
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlotToContainer(new SlotPhantomUnstackable(te.getFilterHandler(), i * 3 + j, 115 + j * 18, 25 + i * 18) {
                    @Override
                    public boolean isItemValid(@Nonnull ItemStack stack) {
                        return true;
                    }
                });
            }
        }
    }
}
