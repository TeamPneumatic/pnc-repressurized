package pneumaticCraft.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.render.pneumaticArmor.DroneDebugUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.common.item.ItemGPSTool;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class AreaShowManager{
    private static AreaShowManager INSTANCE = new AreaShowManager();
    private final Map<ChunkPosition, AreaShowHandler> showHandlers = new HashMap<ChunkPosition, AreaShowHandler>();
    private World world;
    private DroneDebugUpgradeHandler droneDebugger;

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
        for(AreaShowHandler handler : showHandlers.values()) {
            handler.render();
        }

        ItemStack curItem = player.getCurrentEquippedItem();
        if(curItem != null && curItem.getItem() == Itemss.GPSTool) {
            ChunkPosition gpsLocation = ItemGPSTool.getGPSLocation(curItem);
            if(gpsLocation != null) {
                Set<ChunkPosition> set = new HashSet<ChunkPosition>();
                set.add(gpsLocation);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                new AreaShowHandler(set, 0xFFFF00).render();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }

        ItemStack helmet = player.getCurrentArmor(3);
        if(helmet != null && helmet.getItem() == Itemss.pneumaticHelmet) {
            if(droneDebugger == null) droneDebugger = HUDHandler.instance().getSpecificRenderer(DroneDebugUpgradeHandler.class);
            Set<ChunkPosition> set = droneDebugger.getShowingPositions();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            new AreaShowHandler(set, 0xFF0000).render();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
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
        if(areaShower == null) return null;
        removeHandlers(areaShower);
        AreaShowHandler handler = new AreaShowHandler(area, color);
        showHandlers.put(new ChunkPosition(areaShower.xCoord, areaShower.yCoord, areaShower.zCoord), handler);
        return handler;
    }

    public boolean isShowing(TileEntity te){
        return showHandlers.containsKey(new ChunkPosition(te.xCoord, te.yCoord, te.zCoord));
    }

    public void removeHandlers(TileEntity te){
        showHandlers.remove(new ChunkPosition(te.xCoord, te.yCoord, te.zCoord));
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event){
        EntityPlayer player = PneumaticCraft.proxy.getPlayer();
        if(player != null) {
            if(player.worldObj != world) {
                world = player.worldObj;
                showHandlers.clear();
            } else {
                if(event.phase == TickEvent.Phase.END) {
                    Iterator<ChunkPosition> iterator = showHandlers.keySet().iterator();
                    while(iterator.hasNext()) {
                        ChunkPosition pos = iterator.next();
                        if(PneumaticCraftUtils.distBetween(pos, player.posX, player.posY, player.posZ) < 32 && world.isAirBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ)) iterator.remove();
                    }
                }
            }
        }
    }
}
