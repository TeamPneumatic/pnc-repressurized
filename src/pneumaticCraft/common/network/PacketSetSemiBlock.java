package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketSetSemiBlock extends LocationIntPacket<PacketSetSemiBlock>{

    private String id;

    public PacketSetSemiBlock(){}

    public PacketSetSemiBlock(ISemiBlock semiBlock){
        this(semiBlock.getPos(), semiBlock);
    }

    public PacketSetSemiBlock(ChunkPosition pos, ISemiBlock semiBlock){
        super(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        if(semiBlock != null) {
            id = SemiBlockManager.getKeyForSemiBlock(semiBlock);
        } else {
            id = "";
        }
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, id);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        id = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void handleClientSide(PacketSetSemiBlock message, EntityPlayer player){
        SemiBlockManager.getInstance(player.worldObj).setSemiBlock(player.worldObj, message.x, message.y, message.z, message.id.equals("") ? null : SemiBlockManager.getSemiBlockForKey(message.id));
    }

    @Override
    public void handleServerSide(PacketSetSemiBlock message, EntityPlayer player){

    }

}
