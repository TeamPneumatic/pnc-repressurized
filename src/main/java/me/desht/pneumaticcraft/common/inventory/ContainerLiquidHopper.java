package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerLiquidHopper extends ContainerPneumaticBase<TileEntityLiquidHopper> {

    public ContainerLiquidHopper(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerLiquidHopper(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.LIQUID_HOPPER.get(), i, playerInventory, pos);

        addUpgradeSlots(48, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
