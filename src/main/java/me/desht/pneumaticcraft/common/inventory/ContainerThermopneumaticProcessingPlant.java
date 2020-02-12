package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerThermopneumaticProcessingPlant extends
        ContainerPneumaticBase<TileEntityThermopneumaticProcessingPlant> {

    public ContainerThermopneumaticProcessingPlant(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerThermopneumaticProcessingPlant(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.THERMOPNEUMATIC_PROCESSING_PLANT.get(), windowId, playerInventory, pos);

        // add upgrade slots
        for (int i = 0; i < 4; i++) {
            addSlot(new SlotUpgrade(te, i, 80 + i * 18, 93));
        }
        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 38, 14));
        addSlot(new SlotOutput(te.getOutputInventory(), 0, 53, 62));

        addPlayerSlots(playerInventory, 115);
    }
}
