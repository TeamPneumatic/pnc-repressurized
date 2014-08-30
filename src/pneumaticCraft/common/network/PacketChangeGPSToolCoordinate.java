package pneumaticCraft.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.item.Itemss;

public class PacketChangeGPSToolCoordinate extends LocationIntPacket<PacketChangeGPSToolCoordinate>{

    public PacketChangeGPSToolCoordinate(){}

    public PacketChangeGPSToolCoordinate(int x, int y, int z){
        super(x, y, z);
    }

    @Override
    public void handleClientSide(PacketChangeGPSToolCoordinate message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketChangeGPSToolCoordinate message, EntityPlayer player){
        ItemStack playerStack = player.getCurrentEquippedItem();
        if(playerStack != null && playerStack.getItem() == Itemss.GPSTool) {
            playerStack.getItem().onItemUse(playerStack, player, player.worldObj, message.x, message.y, message.z, 0, 0, 0, 0);
        }
    }

}
