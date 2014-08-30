package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketUseItem extends AbstractPacket<PacketUseItem>{

    private Item item;
    private int amount;

    public PacketUseItem(){}

    public PacketUseItem(Item item, int amount){
        this.item = item;
        this.amount = amount;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        ByteBufUtils.writeItemStack(buffer, new ItemStack(item, amount, 0));
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        ItemStack stack = ByteBufUtils.readItemStack(buffer);
        item = stack.getItem();
        amount = stack.stackSize;
    }

    @Override
    public void handleClientSide(PacketUseItem message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketUseItem message, EntityPlayer player){
        for(int i = 0; i < message.amount; i++)
            player.inventory.consumeInventoryItem(message.item);
    }

}
