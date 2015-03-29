package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketUpdateRemoteLayout extends AbstractPacket<PacketUpdateRemoteLayout>{

    private NBTTagCompound layout;

    public PacketUpdateRemoteLayout(){}

    public PacketUpdateRemoteLayout(NBTTagCompound layout){
        this.layout = layout;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        layout = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeTag(buf, layout);
    }

    @Override
    public void handleClientSide(PacketUpdateRemoteLayout message, EntityPlayer player){

    }

    @Override
    public void handleServerSide(PacketUpdateRemoteLayout message, EntityPlayer player){
        ItemStack remote = player.getCurrentEquippedItem();
        if(remote != null && remote.getItem() == Itemss.remote) {
            NBTTagCompound tag = remote.getTagCompound();
            if(tag == null) {
                tag = new NBTTagCompound();
                remote.setTagCompound(tag);
            }
            tag.setTag("actionWidgets", message.layout.getTagList("actionWidgets", 10));
        }
    }

}
