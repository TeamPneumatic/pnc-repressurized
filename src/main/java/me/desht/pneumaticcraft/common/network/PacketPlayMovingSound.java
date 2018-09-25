package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketPlayMovingSound extends AbstractPacket<PacketPlayMovingSound> {

    enum SourceType { ENTITY, STATIC_POS }

    private MovingSounds.Sound sound;
    private int entityId;
    private SourceType sourceType;
    private BlockPos pos;

    public PacketPlayMovingSound() {
    }

    public PacketPlayMovingSound(MovingSounds.Sound sound, Object soundSource) {
        this.sound = sound;
        if (soundSource instanceof Entity) {
            this.sourceType = SourceType.ENTITY;
            this.entityId = ((Entity) soundSource).getEntityId();
        } else if (soundSource instanceof TileEntity) {
            this.sourceType = SourceType.STATIC_POS;
            this.pos = ((TileEntity) soundSource).getPos();
        } else if (soundSource instanceof BlockPos) {
            this.sourceType = SourceType.STATIC_POS;
            this.pos = new BlockPos((BlockPos) soundSource);
        } else {
            throw new IllegalArgumentException("invalid sound source: " + soundSource);
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(sound.ordinal());
        buffer.writeByte(sourceType.ordinal());
        if (sourceType == SourceType.ENTITY) {
            buffer.writeInt(entityId);
        } else if (sourceType == SourceType.STATIC_POS) {
            buffer.writeInt(pos.getX());
            buffer.writeInt(pos.getY());
            buffer.writeInt(pos.getZ());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        sound = MovingSounds.Sound.values()[buffer.readByte()];
        sourceType = SourceType.values()[buffer.readByte()];
        if (sourceType == SourceType.ENTITY) {
            entityId = buffer.readInt();
        } else if (sourceType == SourceType.STATIC_POS) {
            pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }
    }

    @Override
    public void handleClientSide(PacketPlayMovingSound message, EntityPlayer player) {
        if (sourceType == SourceType.ENTITY) {
            Entity e = player.world.getEntityByID(entityId);
            if (e != null) {
                MovingSounds.playMovingSound(sound, e);
            }
        } else if (sourceType == SourceType.STATIC_POS) {
            MovingSounds.playMovingSound(sound, pos);
        }
    }

    @Override
    public void handleServerSide(PacketPlayMovingSound message, EntityPlayer player) {

    }
}
