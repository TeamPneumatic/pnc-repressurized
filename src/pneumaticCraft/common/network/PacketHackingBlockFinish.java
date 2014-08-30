package pneumaticCraft.common.network;

import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.util.WorldAndCoord;

public class PacketHackingBlockFinish extends LocationIntPacket<PacketHackingBlockFinish>{

    public PacketHackingBlockFinish(){}

    public PacketHackingBlockFinish(int x, int y, int z){
        super(x, y, z);
    }

    public PacketHackingBlockFinish(WorldAndCoord coord){
        super(coord.x, coord.y, coord.z);
    }

    @Override
    public void handleClientSide(PacketHackingBlockFinish message, EntityPlayer player){
        IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(player.worldObj, message.x, message.y, message.z, player);
        if(hackableBlock != null) {
            hackableBlock.onHackFinished(player.worldObj, message.x, message.y, message.z, player);
            PneumaticCraft.proxy.getHackTickHandler().trackBlock(new WorldAndCoord(player.worldObj, message.x, message.y, message.z), hackableBlock);
            CommonHUDHandler.getHandlerForPlayer(player).setHackedBlock(null);
            player.worldObj.playSound(message.x, message.y, message.z, "PneumaticCraft:helmetHackFinish", 1.0F, 1.0F, false);
        }
    }

    @Override
    public void handleServerSide(PacketHackingBlockFinish message, EntityPlayer player){}

}
