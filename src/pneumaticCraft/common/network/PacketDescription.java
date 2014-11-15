package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.inventory.SyncedField;
import pneumaticCraft.common.tileentity.TileEntityBase;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketDescription extends LocationIntPacket<PacketDescription>{
    private byte[] types;
    private Object[] values;
    private NBTTagCompound extraData;

    public PacketDescription(){}

    public PacketDescription(TileEntityBase te){
        super(te.xCoord, te.yCoord, te.zCoord);
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
        int dataAmount = buf.readInt();
        types = new byte[dataAmount];
        values = new Object[dataAmount];
        for(int i = 0; i < dataAmount; i++) {
            types[i] = buf.readByte();
            values[i] = PacketUpdateGui.readField(buf, types[i]);
        }
        extraData = ByteBufUtils.readTag(buf);
    }

    @Override
    public void handleClientSide(PacketDescription message, EntityPlayer player){
        TileEntity te = message.getTileEntity(player.worldObj);
        if(te instanceof TileEntityBase) {
            List<SyncedField> descFields = ((TileEntityBase)te).getDescriptionFields();
            if(descFields != null && descFields.size() == message.types.length) {
                for(int i = 0; i < descFields.size(); i++) {
                    descFields.get(i).setValue(message.values[i]);
                }
            }
            ((TileEntityBase)te).readFromPacket(message.extraData);
            ((TileEntityBase)te).onDescUpdate();
        }
    }

    @Override
    public void handleServerSide(PacketDescription message, EntityPlayer player){}

}
