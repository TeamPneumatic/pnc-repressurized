package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketSyncDroneEntityProgWidgets extends AbstractPacket<PacketSyncDroneEntityProgWidgets>{

    private List<IProgWidget> progWidgets;
    private int entityId;

    public PacketSyncDroneEntityProgWidgets(){

    }

    public PacketSyncDroneEntityProgWidgets(EntityDrone drone){
        progWidgets = drone.getProgWidgets();
        entityId = drone.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf){
        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(ByteBufUtils.readTag(buf));
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf){
        NBTTagCompound tag = new NBTTagCompound();
        TileEntityProgrammer.setWidgetsToNBT(progWidgets, tag);
        ByteBufUtils.writeTag(buf, tag);

        buf.writeInt(entityId);
    }

    @Override
    public void handleClientSide(PacketSyncDroneEntityProgWidgets message, EntityPlayer player){
        Entity entity = player.worldObj.getEntityByID(message.entityId);
        if(entity instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone)entity;
            List<IProgWidget> widgets = drone.getProgWidgets();
            widgets.clear();
            widgets.addAll(message.progWidgets);
        }
    }

    @Override
    public void handleServerSide(PacketSyncDroneEntityProgWidgets message, EntityPlayer player){}

}
