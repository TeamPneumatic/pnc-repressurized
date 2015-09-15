package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;

public class PacketProgrammerUpdate extends LocationIntPacket<PacketProgrammerUpdate>{
    private NBTTagCompound progWidgets;

    public PacketProgrammerUpdate(){}

    public PacketProgrammerUpdate(TileEntityProgrammer te){
        super(te.xCoord, te.yCoord, te.zCoord);
        progWidgets = new NBTTagCompound();
        te.writeProgWidgetsToNBT(progWidgets);
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        try {
            new PacketBuffer(buffer).writeNBTTagCompoundToBuffer(progWidgets);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        try {
            progWidgets = new PacketBuffer(buffer).readNBTTagCompoundFromBuffer();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClientSide(PacketProgrammerUpdate message, EntityPlayer player){
        handleServerSide(null, player);
    }

    @Override
    public void handleServerSide(PacketProgrammerUpdate message, EntityPlayer player){
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(te instanceof TileEntityProgrammer) {
            ((TileEntityProgrammer)te).readProgWidgetsFromNBT(message.progWidgets);
            ((TileEntityProgrammer)te).saveToHistory();
            if(!player.worldObj.isRemote) {
                updateOtherWatchingPlayers((TileEntityProgrammer)te, player);
            }
        }
    }

    private void updateOtherWatchingPlayers(TileEntityProgrammer te, EntityPlayer changingPlayer){
        List<EntityPlayerMP> players = changingPlayer.worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, AxisAlignedBB.getBoundingBox(x - 5, y - 5, z - 5, x + 6, y + 6, z + 6));
        for(EntityPlayerMP player : players) {
            if(player != changingPlayer) {
                NetworkHandler.sendTo(new PacketProgrammerUpdate(te), player);
            }
        }
    }

}
