package pneumaticCraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.item.ItemGPSTool;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class AreaShowManager{
    private static AreaShowManager INSTANCE = new AreaShowManager();
    private final List<AreaShowHandler> showHandlers = new ArrayList<AreaShowHandler>();

    public static AreaShowManager getInstance(){
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event){
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.thePlayer;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //    GL11.glDisable(GL11.GL_DEPTH_TEST);
        for(AreaShowHandler handler : showHandlers) {
            handler.render();
        }

        ItemStack curItem = Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem();
        if(curItem != null && curItem.getItem() == Itemss.GPSTool) {
            ChunkPosition gpsLocation = ItemGPSTool.getGPSLocation(curItem);
            if(gpsLocation != null) {
                Set<ChunkPosition> set = new HashSet<ChunkPosition>();
                set.add(gpsLocation);
                new AreaShowHandler(set, 0x00FF00, null).render();
            }
        }

        // GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public AreaShowHandler showArea(ChunkPosition[] area, int color, TileEntity areaShower){
        return showArea(new HashSet<ChunkPosition>(Arrays.asList(area)), color, areaShower);
    }

    public AreaShowHandler showArea(Set<ChunkPosition> area, int color, TileEntity areaShower){
        removeHandlers(areaShower);
        AreaShowHandler handler = new AreaShowHandler(area, color, areaShower);
        showHandlers.add(handler);
        return handler;
    }

    public boolean isShowing(TileEntity te){
        for(AreaShowHandler handler : showHandlers)
            if(handler.areaShower == te) return true;
        return false;
    }

    public void removeHandler(AreaShowHandler handler){
        showHandlers.remove(handler);
    }

    public void removeHandlers(TileEntity areaShower){
        Iterator<AreaShowHandler> iterator = showHandlers.iterator();
        while(iterator.hasNext()) {
            AreaShowHandler showHandler = iterator.next();
            if(showHandler.areaShower == areaShower) iterator.remove();
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.END) {
            Iterator<AreaShowHandler> iterator = showHandlers.iterator();
            while(iterator.hasNext()) {
                AreaShowHandler showHandler = iterator.next();
                if(showHandler.areaShower != null && showHandler.areaShower.isInvalid()) iterator.remove();
            }
        }
    }

}
