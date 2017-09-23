package igwmod;

import java.io.File;

import igwmod.lib.IGWLog;
import igwmod.network.MessageSendServerTab;
import igwmod.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

public class ServerProxy implements IProxy{

    @Override
    public void preInit(FMLPreInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event){
        if(!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
            File serverFolder = new File(IGWMod.proxy.getSaveLocation() + "\\igwmod\\");
            if(!serverFolder.exists()) {
                serverFolder = new File(IGWMod.proxy.getSaveLocation() + "\\igwmodServer\\");//TODO legacy remove
                if(serverFolder.exists()) {
                    IGWLog.warning("Found IGW Mod server page in the 'igwmodServer' folder. This is deprecated! Rename the folder to 'igwmod' instead.");
                }
            }
            if(serverFolder.exists()) {
                NetworkHandler.sendTo(new MessageSendServerTab(serverFolder), (EntityPlayerMP)event.getEntity());
            }
        }
    }

    @Override
    public void postInit(){

    }

    @Override
    public void processIMC(IMCEvent event){}

    @Override
    public String getSaveLocation(){
        String mcDataLocation = FMLServerHandler.instance().getSavesDirectory().getAbsolutePath();
        return mcDataLocation.substring(0, mcDataLocation.length() - 2);
    }

    @Override
    public EntityPlayer getPlayer(){
        return null;
    }

}
