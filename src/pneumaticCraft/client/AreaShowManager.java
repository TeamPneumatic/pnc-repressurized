package pneumaticCraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

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
        for(AreaShowHandler handler : showHandlers) {
            handler.render();
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    public AreaShowHandler showArea(ChunkPosition[] area, int color){
        return showArea(new HashSet<ChunkPosition>(Arrays.asList(area)), color);
    }

    public AreaShowHandler showArea(Set<ChunkPosition> area, int color){
        AreaShowHandler handler = new AreaShowHandler(area, color);
        showHandlers.add(handler);
        return handler;
    }

    public void removeHandler(AreaShowHandler handler){
        showHandlers.remove(handler);
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.END) {
            for(int i = 0; i < showHandlers.size(); i++) {
                if(!showHandlers.get(i).update()) {
                    showHandlers.remove(i);
                    i--;
                }
            }
        }
    }

}
