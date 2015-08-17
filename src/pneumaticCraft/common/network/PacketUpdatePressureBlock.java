package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;

public class PacketUpdatePressureBlock extends LocationIntPacket<PacketUpdatePressureBlock>{
    private int currentAir;

    public PacketUpdatePressureBlock(){}

    public PacketUpdatePressureBlock(TileEntityPneumaticBase te){
        super(te.xCoord, te.yCoord, te.zCoord);
        currentAir = te.currentAir;
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        buf.writeInt(currentAir);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        currentAir = buf.readInt();
    }

    @Override
    public void handleClientSide(PacketUpdatePressureBlock message, EntityPlayer player){
        TileEntity te = message.getTileEntity(player.worldObj);
        if(te instanceof TileEntityPneumaticBase) {
            ((TileEntityPneumaticBase)te).currentAir = message.currentAir;
        } else {
            TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(te);
            if(tube != null) tube.currentAir = message.currentAir;
        }
    }

    @Override
    public void handleServerSide(PacketUpdatePressureBlock message, EntityPlayer player){}

}
