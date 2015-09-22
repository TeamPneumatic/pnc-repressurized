package pneumaticCraft.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.gui.INeedTickUpdate;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.common.block.tubes.ModuleRegulatorTube;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientTickHandler{
    private static final ClientTickHandler INSTANCE = new ClientTickHandler();
    public static int TICKS;

    private final List<WeakReference<INeedTickUpdate>> updatedObjects = new ArrayList<WeakReference<INeedTickUpdate>>();//using weak references so we don't create a memory leak of unused GuiAnimatedStats.

    public static ClientTickHandler instance(){
        return INSTANCE;
    }

    /**
     * Invoking this method will result the given stat to be updated every tick.
     * @param stat
     */
    public void registerUpdatedObject(INeedTickUpdate stat){
        updatedObjects.add(new WeakReference<INeedTickUpdate>(stat));
    }

    /**
     * Method used to force an object to not get updates any longer. When further updates aren't harmful when the object
     * not longer is needed, this method isn't necessary to be used, as the garbage collector will collect the
     * (weak referenced) objects.
     * @param stat
     */
    public void removeUpdatedObject(INeedTickUpdate stat){
        for(int i = 0; i < updatedObjects.size(); i++) {
            if(stat.equals(updatedObjects.get(i).get())) {
                updatedObjects.remove(i);
                break;
            }
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.END) {
            if(Minecraft.getMinecraft().thePlayer == null) {
                for(IUpgradeRenderHandler handler : UpgradeRenderHandlerList.instance().upgradeRenderers) {
                    handler.reset();
                }
            }
            TICKS++;
            ModuleRegulatorTube.hasTicked = false;
            ModuleRegulatorTube.inverted = false;
            ModuleRegulatorTube.inLine = true;
            for(int i = 0; i < updatedObjects.size(); i++) {
                INeedTickUpdate updatedObject = updatedObjects.get(i).get();
                if(updatedObject != null) {
                    updatedObject.update();
                } else {
                    updatedObjects.remove(i);
                    i--;
                }
            }
        }
    }
}
