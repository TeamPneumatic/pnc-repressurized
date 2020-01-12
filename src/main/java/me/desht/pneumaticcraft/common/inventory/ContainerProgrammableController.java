package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerProgrammableController extends ContainerPneumaticBase<TileEntityProgrammableController> {

    public ContainerProgrammableController(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerProgrammableController(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.PROGRAMMABLE_CONTROLLER.get(), i, playerInventory, pos);

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 71, 36));

        addUpgradeSlots(21, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
