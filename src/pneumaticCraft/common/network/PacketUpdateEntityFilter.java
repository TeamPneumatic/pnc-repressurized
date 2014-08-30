package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.NBTUtil;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketUpdateEntityFilter extends AbstractPacket<PacketUpdateEntityFilter>{

    private String filter;

    public PacketUpdateEntityFilter(){}

    public PacketUpdateEntityFilter(String filter){
        this.filter = filter;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        ByteBufUtils.writeUTF8String(buffer, filter);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        filter = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handleClientSide(PacketUpdateEntityFilter message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketUpdateEntityFilter message, EntityPlayer player){
        ItemStack stack = player.inventory.armorItemInSlot(3);
        if(stack != null) {
            NBTUtil.setString(stack, "entityFilter", message.filter);
        }
    }

}
