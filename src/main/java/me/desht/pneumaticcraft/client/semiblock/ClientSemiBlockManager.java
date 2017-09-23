package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class ClientSemiBlockManager {
    private static final Map<Class<? extends ISemiBlock>, ISemiBlockRenderer> renderers = new HashMap<Class<? extends ISemiBlock>, ISemiBlockRenderer>();

    static {
        registerRenderer(SemiBlockLogistics.class, new SemiBlockRendererLogistics());
        registerRenderer(SemiBlockHeatFrame.class, new SemiBlockRendererHeatFrame());
    }

    public static void registerRenderer(Class<? extends ISemiBlock> semiBlock, ISemiBlockRenderer renderer) {
        renderers.put(semiBlock, renderer);
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
        RenderHelper.enableStandardItemLighting();

        for (Map<BlockPos, ISemiBlock> map : SemiBlockManager.getInstance(player.world).getSemiBlocks().values()) {
            for (ISemiBlock semiBlock : map.values()) {
                ISemiBlockRenderer renderer = getRenderer(semiBlock);
                if (renderer != null) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(semiBlock.getPos().getX(), semiBlock.getPos().getY(), semiBlock.getPos().getZ());
                    renderer.render(semiBlock, event.getPartialTicks());
                    GL11.glPopMatrix();
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }

    public static ISemiBlockRenderer getRenderer(ISemiBlock semiBlock) {
        Class clazz = semiBlock.getClass();
        while (clazz != Object.class && !renderers.containsKey(clazz)) {
            clazz = clazz.getSuperclass();
        }
        return renderers.get(clazz);
    }
}
