package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class ContainerSecurityStationHacking extends ContainerPneumaticBase<TileEntitySecurityStation> {
    public static final int NODE_SPACING = 31;

    public ContainerSecurityStationHacking(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerSecurityStationHacking(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.SECURITY_STATION_HACKING.get(), windowId, playerInventory, pos);

        //add the network slots
        for (int i = 0; i < TileEntitySecurityStation.INV_ROWS; i++) {
            for (int j = 0; j < TileEntitySecurityStation.INV_COLS; j++) {
                SlotUntouchable slot = (SlotUntouchable) addSlot(new SlotUntouchable(te.getPrimaryInventory(), j + i * 5, 8 + j * NODE_SPACING, 22 + i * NODE_SPACING));
                slot.setEnabled(slot.getHasStack());
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

}
