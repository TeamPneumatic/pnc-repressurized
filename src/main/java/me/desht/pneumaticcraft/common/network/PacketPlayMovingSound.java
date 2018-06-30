package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PacketPlayMovingSound extends AbstractPacket<PacketPlayMovingSound> {
    private MovingSounds.Sound sound;
    private int entityId;

    public PacketPlayMovingSound() {
    }

    public PacketPlayMovingSound(MovingSounds.Sound sound, Entity entity) {
        this.sound = sound;
        this.entityId = entity.getEntityId();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(sound.ordinal());
        buffer.writeInt(entityId);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        sound = MovingSounds.Sound.values()[buffer.readByte()];
        entityId = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketPlayMovingSound message, EntityPlayer player) {
        Entity e = player.world.getEntityByID(entityId);
        if (e != null) {
            MovingSounds.playMovingSound(sound, e);
        }
    }

    @Override
    public void handleServerSide(PacketPlayMovingSound message, EntityPlayer player) {

    }
}
