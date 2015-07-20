package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.common.inventory.ContainerLogistics;
import pneumaticCraft.common.inventory.SyncedField;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketDescription extends LocationIntPacket<PacketDescription>{
    private byte[] types;
    private Object[] values;
    private NBTTagCompound extraData;
    private IDescSynced.Type type;

    public PacketDescription(){}

    public PacketDescription(IDescSynced te){
        super(te.getX(), te.getY(), te.getZ());
        type = te.getSyncType();
        values = new Object[te.getDescriptionFields().size()];
        types = new byte[values.length];
        for(int i = 0; i < values.length; i++) {
            values[i] = te.getDescriptionFields().get(i).getValue();
            types[i] = PacketUpdateGui.getType(te.getDescriptionFields().get(i));
        }
        extraData = new NBTTagCompound();
        te.writeToPacket(extraData);
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        buf.writeByte(type.ordinal());
        buf.writeInt(values.length);
        for(int i = 0; i < types.length; i++) {
            buf.writeByte(types[i]);
            PacketUpdateGui.writeField(buf, values[i], types[i]);
        }
        ByteBufUtils.writeTag(buf, extraData);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        type = IDescSynced.Type.values()[buf.readByte()];
        int dataAmount = buf.readInt();
        types = new byte[dataAmount];
        values = new Object[dataAmount];
        for(int i = 0; i < dataAmount; i++) {
            types[i] = buf.readByte();
            values[i] = PacketUpdateGui.readField(buf, types[i]);
        }
        extraData = ByteBufUtils.readTag(buf);
    }

    public static Object getSyncableForType(LocationIntPacket message, EntityPlayer player, IDescSynced.Type type){
        switch(type){
            case TILE_ENTITY:
                return message.getTileEntity(player.worldObj);
            case SEMI_BLOCK:
                if(message.x == 0 && message.y == 0 && message.z == 0) {
                    Container container = player.openContainer;
                    if(container instanceof ContainerLogistics) {
                        return ((ContainerLogistics)container).logistics;
                    }
                } else {
                    return SemiBlockManager.getInstance(player.worldObj).getSemiBlock(player.worldObj, message.x, message.y, message.z);
                }
        }
        return null;
    }

    @Override
    public void handleClientSide(PacketDescription message, EntityPlayer player){
        if(player.worldObj.blockExists(message.x, message.y, message.z)) {
            Object syncable = getSyncableForType(message, player, message.type);
            if(syncable instanceof IDescSynced) {
                IDescSynced descSynced = (IDescSynced)syncable;
                List<SyncedField> descFields = descSynced.getDescriptionFields();
                if(descFields != null && descFields.size() == message.types.length) {
                    for(int i = 0; i < descFields.size(); i++) {
                        descFields.get(i).setValue(message.values[i]);
                    }
                }
                descSynced.readFromPacket(message.extraData);
                descSynced.onDescUpdate();
            }
        }
    }

    @Override
    public void handleServerSide(PacketDescription message, EntityPlayer player){}

}
