package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * The primary mechanism for sync'ing data to an open GUI
 */
public class PacketUpdateGui {
    private int syncId;
    private Object value;
    private byte type;

    public PacketUpdateGui() {
    }

    public PacketUpdateGui(int syncId, SyncedField syncField) {
        this.syncId = syncId;
        value = syncField.getValue();
        type = SyncedField.getType(syncField);
    }

    public PacketUpdateGui(PacketBuffer buf) {
        syncId = buf.readInt();
        type = buf.readByte();
        value = SyncedField.fromBytes(buf, type);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(syncId);
        buf.writeByte(type);
        SyncedField.toBytes(buf, value, type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Container container = ctx.get().getSender().openContainer;
            if (container instanceof ContainerPneumaticBase) {
                ((ContainerPneumaticBase) container).updateField(syncId, value);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
