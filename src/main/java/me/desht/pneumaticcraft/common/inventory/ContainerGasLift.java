package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerGasLift extends ContainerPneumaticBase<TileEntityGasLift> {

    public ContainerGasLift(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerGasLift(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.GAS_LIFT, i, playerInventory, pos);

        addUpgradeSlots(11, 29);

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 55, 48));

        addPlayerSlots(playerInventory, 84);
    }
}
