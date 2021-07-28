package me.desht.pneumaticcraft.common.inventory;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Collections;
import java.util.List;

public class ContainerUniversalSensor extends ContainerPneumaticBase<TileEntityUniversalSensor> {
    private final List<String> globalVars;

    public ContainerUniversalSensor(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.UNIVERSAL_SENSOR.get(), windowId, playerInventory, pos);

        addUpgradeSlots(19, 108);

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 29, 72));

        addPlayerSlots(playerInventory, 157);

        globalVars = Collections.emptyList();
    }

    public ContainerUniversalSensor(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(ModContainers.UNIVERSAL_SENSOR.get(), windowId, playerInventory, buffer.readBlockPos());

        int nVars = buffer.readVarInt();
        ImmutableList.Builder<String> b = ImmutableList.builder();
        for (int i = 0; i < nVars; i++) {
             b.add(buffer.readString());
        }
        globalVars = b.build();

        addUpgradeSlots(19, 108);

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 29, 72));

        addPlayerSlots(playerInventory, 157);
    }

    public List<String> getGlobalVars() {
        return globalVars;
    }
}
