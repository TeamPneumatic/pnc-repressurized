package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSpawnRing extends LocationDoublePacket<PacketSpawnRing> {

    private int[] colors;
    private int targetEntityId;

    public PacketSpawnRing() {
    }

    public PacketSpawnRing(double x, double y, double z, Entity targetEntity, int... colors) {
        super(x, y, z);
        targetEntityId = targetEntity.getEntityId();
        this.colors = colors;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(targetEntityId);
        buffer.writeInt(colors.length);
        for (int i : colors) {
            buffer.writeInt(i);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        targetEntityId = buffer.readInt();
        colors = new int[buffer.readInt()];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = buffer.readInt();
        }
    }

    @Override
    public void handleClientSide(PacketSpawnRing message, EntityPlayer player) {
        Entity entity = player.world.getEntityByID(message.targetEntityId);
        if (entity != null) {
            for (int color : message.colors) {
                player.world.spawnEntity(new EntityRing(player.world, message.x, message.y, message.z, entity, color));
            }
        }
    }

    @Override
    public void handleServerSide(PacketSpawnRing message, EntityPlayer player) {
    }

}
