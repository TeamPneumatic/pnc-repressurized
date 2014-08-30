package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pneumaticCraft.common.NBTUtil;

public class PacketCoordTrackUpdate extends LocationIntPacket<PacketCoordTrackUpdate>{

    int dimensionID;

    public PacketCoordTrackUpdate(){}

    public PacketCoordTrackUpdate(World world, int x, int y, int z){
        super(x, y, z);
        dimensionID = world.provider.dimensionId;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(dimensionID);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        dimensionID = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketCoordTrackUpdate message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketCoordTrackUpdate message, EntityPlayer player){
        ItemStack stack = player.inventory.armorItemInSlot(3);
        if(stack != null) {
            NBTTagCompound tag = NBTUtil.getCompoundTag(stack, "CoordTracker");
            tag.setInteger("dimID", message.dimensionID);
            tag.setInteger("x", message.x);
            tag.setInteger("y", message.y);
            tag.setInteger("z", message.z);
        }
    }

}
