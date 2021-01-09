package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to start a new MovingSound playing
 */
public class PacketPlayMovingSound {
    enum SourceType { ENTITY, STATIC_POS}

    private final MovingSounds.Sound sound;
    private final BlockPos pos;
    private final int entityId;
    private final SourceType sourceType;

    public PacketPlayMovingSound(MovingSounds.Sound sound, Object soundSource) {
        this.sound = sound;
        if (soundSource instanceof Entity) {
            this.sourceType = SourceType.ENTITY;
            this.entityId = ((Entity) soundSource).getEntityId();
            this.pos = null;
        } else if (soundSource instanceof TileEntity) {
            this.sourceType = SourceType.STATIC_POS;
            this.pos = ((TileEntity) soundSource).getPos();
            this.entityId = -1;
        } else if (soundSource instanceof BlockPos) {
            this.sourceType = SourceType.STATIC_POS;
            this.pos = new BlockPos((BlockPos) soundSource);
            this.entityId = -1;
        } else {
            throw new IllegalArgumentException("invalid sound source: " + soundSource);
        }
    }

    public PacketPlayMovingSound(PacketBuffer buffer) {
        sound = MovingSounds.Sound.values()[buffer.readByte()];
        sourceType = SourceType.values()[buffer.readByte()];
        if (sourceType == SourceType.ENTITY) {
            entityId = buffer.readInt();
            pos = null;
        } else if (sourceType == SourceType.STATIC_POS) {
            pos = buffer.readBlockPos();
            entityId = -1;
        } else {
            throw new IllegalArgumentException("invalid sound source: " + sourceType);
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(sound.ordinal());
        buffer.writeByte(sourceType.ordinal());
        if (sourceType == SourceType.ENTITY) {
            buffer.writeInt(entityId);
        } else if (sourceType == SourceType.STATIC_POS) {
            buffer.writeBlockPos(pos);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (sourceType == SourceType.ENTITY) {
                Entity e = ClientUtils.getClientWorld().getEntityByID(entityId);
                if (e != null) {
                    MovingSounds.playMovingSound(sound, e);
                }
            } else if (sourceType == SourceType.STATIC_POS) {
                MovingSounds.playMovingSound(sound, pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
