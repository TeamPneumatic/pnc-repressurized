package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.client.render.pneumaticArmor.BlockTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.RenderBlockTarget;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.util.WorldAndCoord;

public class PacketHackingBlockStart extends LocationIntPacket<PacketHackingBlockStart>{

    public PacketHackingBlockStart(){}

    public PacketHackingBlockStart(int x, int y, int z){
        super(x, y, z);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
    }

    @Override
    public void handleClientSide(PacketHackingBlockStart message, EntityPlayer player){
        CommonHUDHandler.getHandlerForPlayer(player).setHackedBlock(new WorldAndCoord(player.worldObj, message.x, message.y, message.z));
        RenderBlockTarget target = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getTargetForCoord(message.x, message.y, message.z);
        if(target != null) target.onHackConfirmServer();
    }

    @Override
    public void handleServerSide(PacketHackingBlockStart message, EntityPlayer player){
        CommonHUDHandler.getHandlerForPlayer(player).setHackedBlock(new WorldAndCoord(player.worldObj, message.x, message.y, message.z));
        NetworkHandler.sendToAllAround(message, player.worldObj);
    }

}
