package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;

public abstract class PacketUpdateTubeModule<REQ extends PacketUpdateTubeModule> extends LocationIntPacket<REQ>{

    protected ForgeDirection moduleSide;

    public PacketUpdateTubeModule(){}

    public PacketUpdateTubeModule(TubeModule module){
        super(module.getTube().x(), module.getTube().y(), module.getTube().z());
        moduleSide = module.getDirection();
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeByte((byte)moduleSide.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        moduleSide = ForgeDirection.getOrientation(buffer.readByte());
    }

    @Override
    public void handleClientSide(REQ message, EntityPlayer player){
        handleServerSide(message, player);
    }

    @Override
    public void handleServerSide(REQ message, EntityPlayer player){
        TileEntityPressureTube te = ModInteractionUtils.getInstance().getTube(player.worldObj.getTileEntity(message.x, message.y, message.z));
        if(te != null) {
            TubeModule module = te.modules[message.moduleSide.ordinal()];
            if(module != null) {
                onModuleUpdate(module, message, player);
                if(!player.worldObj.isRemote) NetworkHandler.sendToAllAround(message, player.worldObj);
            }
        }
    }

    protected abstract void onModuleUpdate(TubeModule module, REQ message, EntityPlayer player);

}
