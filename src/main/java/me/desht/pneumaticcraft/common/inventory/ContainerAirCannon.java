package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerAirCannon extends ContainerPneumaticBase<TileEntityAirCannon> {

    public ContainerAirCannon(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerAirCannon(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.AIR_CANNON.get(), i, playerInventory, pos);

        addUpgradeSlots(8, 29);

        // add the gps slot
        addSlot(new SlotItemSpecific(te.getPrimaryInventory(), ModItems.GPS_TOOL.get(), 1, 51, 29));

        // add the cannoned slot.
        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 79, 40));

        addPlayerSlots(playerInventory, 84);
    }
}
