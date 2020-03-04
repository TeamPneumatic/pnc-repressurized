package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerFluidTank extends ContainerPneumaticBase<TileEntityFluidTank> {
    public ContainerFluidTank(int windowId, PlayerInventory inv, PacketBuffer extraData) {
        this(windowId, inv, getTilePos(extraData));
    }

    public ContainerFluidTank(int windowId, PlayerInventory inv, BlockPos pos) {
        super(ModContainers.FLUID_TANK.get(), windowId, inv, pos);

        addSlot(new SlotFluidContainer(te.getPrimaryInventory(), 0, 132, 22));
        addSlot(new SlotOutput(te.getPrimaryInventory(), 1, 132, 55));

        addUpgradeSlots(23, 29);

        addPlayerSlots(inv, 84);
    }
}
