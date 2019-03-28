package me.desht.pneumaticcraft.common.entity.living;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class DebugEntry /*implements Comparable<DebugEntry>*/ {
    private final int progWidgetId;
    private final String message;
    private final BlockPos pos;
    private long receivedTime; // timestamp for when packet was received on client

    /**
     * Called server side when a debug message is added to a drone.
     *
     * @param message the message text
     * @param progWidgetId a programming widget ID
     * @param pos block position
     */
    DebugEntry(String message, int progWidgetId, BlockPos pos) {
        this.message = message;
        this.pos = pos != null ? pos : BlockPos.ORIGIN;
        this.progWidgetId = progWidgetId;
    }

    /**
     * Called client-side when a message is synced.
     *
     * @param buf message buffer
     */
    public DebugEntry(ByteBuf buf) {
        message = ByteBufUtils.readUTF8String(buf);
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        progWidgetId = buf.readInt();
        receivedTime = System.currentTimeMillis();
    }

    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, message);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(progWidgetId);
    }

    public String getMessage() {
        return message;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getProgWidgetId() {
        return progWidgetId;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public boolean hasCoords() {
        return pos.getX() != 0 || pos.getY() != 0 || pos.getZ() != 0;
    }

}
