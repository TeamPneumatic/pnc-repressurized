package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
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
    enum SourceType { ENTITY, STATIC_POS;}
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

    public PacketPlayMovingSound(PacketBuffer buffer) {
        sound = MovingSounds.Sound.values()[buffer.readByte()];
        sourceType = SourceType.values()[buffer.readByte()];
        if (sourceType == SourceType.ENTITY) {
            entityId = buffer.readInt();
        } else if (sourceType == SourceType.STATIC_POS) {
            pos = buffer.readBlockPos();
        }
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(sound.ordinal());
        buffer.writeByte(sourceType.ordinal());
        if (sourceType == SourceType.ENTITY) {
            buffer.writeInt(entityId);
        } else if (sourceType == SourceType.STATIC_POS) {
            new PacketBuffer(buffer).writeBlockPos(pos);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (sourceType == SourceType.ENTITY) {
                Entity e = PneumaticCraftRepressurized.proxy.getClientWorld().getEntityByID(entityId);
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
