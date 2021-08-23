package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.List;

/**
 * Received on: CLIENT
 * Sent by server to sync a (debugged) drone's programming widgets
 */
public class PacketSyncDroneEntityProgWidgets extends PacketDroneDebugBase {
    private final List<IProgWidget> progWidgets;

    public PacketSyncDroneEntityProgWidgets(IDroneBase drone) {
        super(drone);
        progWidgets = drone.getProgWidgets();
    }

    PacketSyncDroneEntityProgWidgets(PacketBuffer buffer) {
        super(buffer);
        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(buffer.readNbt());
    }

    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeNbt(TileEntityProgrammer.putWidgetsToNBT(progWidgets, new CompoundNBT()));
    }

    @Override
    void handle(PlayerEntity player, IDroneBase droneBase) {
        List<IProgWidget> widgets = droneBase.getProgWidgets();
        widgets.clear();
        widgets.addAll(progWidgets);
    }
}
