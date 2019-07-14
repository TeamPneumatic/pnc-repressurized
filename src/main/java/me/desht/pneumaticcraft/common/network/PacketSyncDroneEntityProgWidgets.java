package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync a (debugged) drone's programming widgets
 */
public class PacketSyncDroneEntityProgWidgets {

    private List<IProgWidget> progWidgets;
    private int entityId;

    public PacketSyncDroneEntityProgWidgets() {
        // empty
    }

    public PacketSyncDroneEntityProgWidgets(EntityDrone drone) {
        progWidgets = drone.getProgWidgets();
        entityId = drone.getEntityId();
    }

    PacketSyncDroneEntityProgWidgets(PacketBuffer buffer) {
        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(buffer.readCompoundTag());
        entityId = buffer.readInt();
    }

    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeCompoundTag(TileEntityProgrammer.setWidgetsToNBT(progWidgets, new CompoundNBT()));
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = PneumaticCraftRepressurized.proxy.getClientWorld().getEntityByID(entityId);
            if (entity instanceof EntityDrone) {
                EntityDrone drone = (EntityDrone) entity;
                List<IProgWidget> widgets = drone.getProgWidgets();
                widgets.clear();
                widgets.addAll(progWidgets);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
