package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class AreaShowManager {
    private static AreaShowManager INSTANCE = new AreaShowManager();
    private final Map<BlockPos, AreaShowHandler> showHandlers = new HashMap<BlockPos, AreaShowHandler>();
    private World world;
    private DroneDebugUpgradeHandler droneDebugger;

    public static AreaShowManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //    GL11.glDisable(GL11.GL_DEPTH_TEST);
        for (AreaShowHandler handler : showHandlers.values()) {
            handler.render();
        }

        ItemStack curItem = player.getHeldItemMainhand();
        if (curItem.getItem() == Itemss.GPS_TOOL) {
            BlockPos gpsLocation = ItemGPSTool.getGPSLocation(curItem);
            if (gpsLocation != null) {
                Set<BlockPos> set = new HashSet<BlockPos>();
                set.add(gpsLocation);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                new AreaShowHandler(set, 0xFFFF00).render();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }

        PlayerArmorInvWrapper armor = new PlayerArmorInvWrapper(player.inventory);
        ItemStack helmet = armor.getStackInSlot(3);
        if (helmet.getItem() == Itemss.PNEUMATIC_HELMET) {
            if (droneDebugger == null)
                droneDebugger = HUDHandler.instance().getSpecificRenderer(DroneDebugUpgradeHandler.class);
            Set<BlockPos> set = droneDebugger.getShowingPositions();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            new AreaShowHandler(set, 0xFF0000).render();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        // GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public AreaShowHandler showArea(BlockPos[] area, int color, TileEntity areaShower) {
        return showArea(new HashSet<BlockPos>(Arrays.asList(area)), color, areaShower);
    }

    public AreaShowHandler showArea(Set<BlockPos> area, int color, TileEntity areaShower) {
        if (areaShower == null) return null;
        removeHandlers(areaShower);
        AreaShowHandler handler = new AreaShowHandler(area, color);
        showHandlers.put(new BlockPos(areaShower.getPos().getX(), areaShower.getPos().getY(), areaShower.getPos().getZ()), handler);
        return handler;
    }

    public boolean isShowing(TileEntity te) {
        return showHandlers.containsKey(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    public void removeHandlers(TileEntity te) {
        showHandlers.remove(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        EntityPlayer player = PneumaticCraftRepressurized.proxy.getPlayer();
        if (player != null) {
            if (player.world != world) {
                world = player.world;
                showHandlers.clear();
            } else {
                if (event.phase == TickEvent.Phase.END) {
                    showHandlers.keySet().removeIf(pos -> PneumaticCraftUtils.distBetween(pos, player.posX, player.posY, player.posZ) < 32 && world.isAirBlock(pos));
                }
            }
        }
    }
}
