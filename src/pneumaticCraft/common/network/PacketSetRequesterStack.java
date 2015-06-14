package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.inventory.ContainerRequester;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.common.semiblock.SemiBlockRequester;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketSetRequesterStack extends LocationIntPacket<PacketSetRequesterStack>{
    private ItemStack settingStack;
    private int settingIndex;

    public PacketSetRequesterStack(){}

    public PacketSetRequesterStack(SemiBlockRequester requester, ItemStack stack, int index){
        super(requester.getPos().chunkPosX, requester.getPos().chunkPosY, requester.getPos().chunkPosZ);
        settingStack = stack;
        settingIndex = index;
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        ByteBufUtils.writeItemStack(buf, settingStack);
        buf.writeInt(settingIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        settingStack = ByteBufUtils.readItemStack(buf);
        settingIndex = buf.readInt();
    }

    @Override
    public void handleClientSide(PacketSetRequesterStack message, EntityPlayer player){

    }

    @Override
    public void handleServerSide(PacketSetRequesterStack message, EntityPlayer player){
        if(message.x == 0 && message.y == 0 && message.z == 0) {
            if(player.openContainer instanceof ContainerRequester) {
                ((ContainerRequester)player.openContainer).requester.getRequests().setInventorySlotContents(message.settingIndex, message.settingStack);
            }
        } else {
            ISemiBlock semiBlock = SemiBlockManager.getInstance().getSemiBlock(player.worldObj, message.x, message.y, message.z);
            if(semiBlock instanceof SemiBlockRequester) {
                ((SemiBlockRequester)semiBlock).getRequests().setInventorySlotContents(message.settingIndex, message.settingStack);
            }
        }
    }

}
