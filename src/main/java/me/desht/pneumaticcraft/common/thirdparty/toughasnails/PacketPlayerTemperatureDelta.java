package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.AirConUpgradeHandler;
import me.desht.pneumaticcraft.common.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;

public class PacketPlayerTemperatureDelta extends AbstractPacket<PacketPlayerTemperatureDelta> {
    private int deltaTemp;

    public PacketPlayerTemperatureDelta() {
    }

    PacketPlayerTemperatureDelta(int deltaTemp) {
        this.deltaTemp = deltaTemp;
    }

    @Override
    public void handleClientSide(PacketPlayerTemperatureDelta message, EntityPlayer player) {
        AirConUpgradeHandler.deltaTemp = message.deltaTemp;
    }

    @Override
    public void handleServerSide(PacketPlayerTemperatureDelta message, EntityPlayer player) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        deltaTemp = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(deltaTemp);
    }
}
