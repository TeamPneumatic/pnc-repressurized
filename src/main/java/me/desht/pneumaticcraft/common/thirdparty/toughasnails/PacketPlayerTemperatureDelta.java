package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.AirConClientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when air conditioning level changes so client can update the HUD gauge
 */
public class PacketPlayerTemperatureDelta {
    private int deltaTemp;

    public PacketPlayerTemperatureDelta() {
        // empty
    }

    PacketPlayerTemperatureDelta(int deltaTemp) {
        this.deltaTemp = deltaTemp;
    }

    PacketPlayerTemperatureDelta(PacketBuffer buffer) {
        deltaTemp = buffer.readByte();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeByte(deltaTemp);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AirConClientHandler.deltaTemp = deltaTemp);
        ctx.get().setPacketHandled(true);
    }
}
