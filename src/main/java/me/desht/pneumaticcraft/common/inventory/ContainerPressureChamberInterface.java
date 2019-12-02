package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerPressureChamberInterface extends ContainerPneumaticBase<TileEntityPressureChamberInterface> {

    public ContainerPressureChamberInterface(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerPressureChamberInterface(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.PRESSURE_CHAMBER_INTERFACE, windowId, playerInventory, pos);

        // add the transfer slot
        addSlot(new SlotUntouchable(te.getPrimaryInventory(), 0, 66, 35));

        addUpgradeSlots(20, 26);

        addPlayerSlots(playerInventory, 84);

        // add the export filter slots
        //  - after the player slots so they won't be shift-clicked.
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                addSlot(new SlotPhantomUnstackable(te.getFilterHandler(), i * 3 + j, 115 + j * 18, 25 + i * 18) {
//                    @Override
//                    public boolean isItemValid(@Nonnull ItemStack stack) {
//                        return true;
//                    }
//                });
//            }
//        }
    }
}
