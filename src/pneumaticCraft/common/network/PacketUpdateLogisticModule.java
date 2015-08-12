package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.tubes.ModuleLogistics;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;

public class PacketUpdateLogisticModule extends LocationIntPacket<PacketUpdateLogisticModule>{

    private int side;
    private int colorIndex;
    private int status;

    public PacketUpdateLogisticModule(){}

    public PacketUpdateLogisticModule(ModuleLogistics logisticsModule, int action){
        super(logisticsModule.getTube().x(), logisticsModule.getTube().y(), logisticsModule.getTube().z());
        side = logisticsModule.getDirection().ordinal();
        colorIndex = logisticsModule.getColorChannel();
        if(action > 0) {
            status = 1 + action;
        } else {
            status = logisticsModule.hasPower() ? 1 : 0;
        }
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        buf.writeByte(side);
        buf.writeByte(colorIndex);
        buf.writeByte(status);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        side = buf.readByte();
        colorIndex = buf.readByte();
        status = buf.readByte();
    }

    @Override
    public void handleClientSide(PacketUpdateLogisticModule message, EntityPlayer player){
        TileEntity te = message.getTileEntity(player.worldObj);
        if(te instanceof TileEntityPressureTube) {
            TubeModule module = ((TileEntityPressureTube)te).modules[message.side];
            if(module instanceof ModuleLogistics) {
                ((ModuleLogistics)module).onUpdatePacket(message.status, message.colorIndex);
            }
        }
    }

    @Override
    public void handleServerSide(PacketUpdateLogisticModule message, EntityPlayer player){

    }

}
