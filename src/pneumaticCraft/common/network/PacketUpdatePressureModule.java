package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.block.tubes.TubeModule;

public class PacketUpdatePressureModule extends PacketUpdateTubeModule<PacketUpdatePressureModule>{
    private int fieldId;
    private float value;

    public PacketUpdatePressureModule(){};

    public PacketUpdatePressureModule(TubeModule module, int fieldId, float value){
        super(module);
        this.fieldId = fieldId;
        this.value = value;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(fieldId);
        buffer.writeFloat(value);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        fieldId = buffer.readInt();
        value = buffer.readFloat();
    }

    @Override
    protected void onModuleUpdate(TubeModule module, PacketUpdatePressureModule message, EntityPlayer player){
        if(message.fieldId == 0) {
            module.lowerBound = message.value;
        } else if(message.fieldId == 1) {
            module.higherBound = message.value;
        } else if(message.fieldId == 2) {
            module.advancedConfig = message.value > 0.5F;
        }
        module.sendDescriptionPacket();
    }
}
