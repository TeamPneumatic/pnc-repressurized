package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerAssemblyController extends ContainerPneumaticBase<TileEntityAssemblyController> {
    public ContainerAssemblyController(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerAssemblyController(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.ASSEMBLY_CONTROLLER, i, playerInventory, pos);

        addSlot(new SlotItemSpecific(te.getPrimaryInventory(), item -> item instanceof ItemAssemblyProgram, 0, 74, 38));

        addUpgradeSlots(13, 31);

        addPlayerSlots(playerInventory, 84);
    }
}
