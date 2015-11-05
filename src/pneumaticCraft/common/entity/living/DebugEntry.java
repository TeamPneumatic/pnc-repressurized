package pneumaticCraft.common.entity.living;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.ChunkPosition;
import cpw.mods.fml.common.network.ByteBufUtils;

public class DebugEntry implements Comparable<DebugEntry>{
    private static int curId;

    private final int id;
    private final int progWidgetId;
    private final String message;
    private final ChunkPosition pos;

    public DebugEntry(String message, int progWidgetId, ChunkPosition pos){
        this.message = message;
        this.pos = pos != null ? pos : new ChunkPosition(0, 0, 0);
        this.progWidgetId = progWidgetId;
        id = curId++;
    }

    public DebugEntry(ByteBuf buf){
        message = ByteBufUtils.readUTF8String(buf);
        pos = new ChunkPosition(buf.readInt(), buf.readInt(), buf.readInt());
        id = buf.readInt();
        progWidgetId = buf.readInt();
    }

    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeUTF8String(buf, message);
        buf.writeInt(pos.chunkPosX);
        buf.writeInt(pos.chunkPosY);
        buf.writeInt(pos.chunkPosZ);
        buf.writeInt(id);
        buf.writeInt(progWidgetId);
    }

    public String getMessage(){
        return message;
    }

    public ChunkPosition getPos(){
        return pos;
    }

    public int getProgWidgetId(){
        return progWidgetId;
    }

    public int hashcode(){
        return id;
    }

    @Override
    public boolean equals(Object other){
        return other instanceof DebugEntry ? ((DebugEntry)other).id == id : false;
    }

    @Override
    public int compareTo(DebugEntry o){
        return Integer.compare(id, o.id);
    }

}
