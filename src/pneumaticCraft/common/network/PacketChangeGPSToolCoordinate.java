package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.ItemGPSTool;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketChangeGPSToolCoordinate extends LocationIntPacket<PacketChangeGPSToolCoordinate>{
    private String variable;

    public PacketChangeGPSToolCoordinate(){}

    public PacketChangeGPSToolCoordinate(int x, int y, int z, String variable){
        super(x, y, z);
        this.variable = variable;
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, variable);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        variable = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void handleClientSide(PacketChangeGPSToolCoordinate message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketChangeGPSToolCoordinate message, EntityPlayer player){
        ItemStack playerStack = player.getCurrentEquippedItem();
        if(playerStack != null && playerStack.getItem() == Itemss.GPSTool) {
            ItemGPSTool.setVariable(playerStack, message.variable);
            if(message.y >= 0) {
                playerStack.getItem().onItemUse(playerStack, player, player.worldObj, message.x, message.y, message.z, 0, 0, 0, 0);
            }
        }
    }
}
