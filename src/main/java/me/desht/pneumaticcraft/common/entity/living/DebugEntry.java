package me.desht.pneumaticcraft.common.entity.living;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class DebugEntry implements Comparable<DebugEntry> {
    private static int curId;

    private final int id;
    private final int progWidgetId;
    private final String message;
    private final BlockPos pos;

    public DebugEntry(String message, int progWidgetId, BlockPos pos) {
        this.message = message;
        this.pos = pos != null ? pos : new BlockPos(0, 0, 0);
        this.progWidgetId = progWidgetId;
        id = curId++;
    }

    public DebugEntry(ByteBuf buf) {
        message = ByteBufUtils.readUTF8String(buf);
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        id = buf.readInt();
        progWidgetId = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, message);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(id);
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

    public int hashcode() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DebugEntry && ((DebugEntry) other).id == id;
    }

    @Override
    public int compareTo(DebugEntry o) {
        return Integer.compare(id, o.id);
    }

}
