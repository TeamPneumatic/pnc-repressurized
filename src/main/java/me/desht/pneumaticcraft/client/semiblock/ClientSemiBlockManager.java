package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.common.semiblock.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSemiBlockManager {
    private static final Map<Class<? extends ISemiBlock>, ISemiBlockRenderer<?>> renderers = new HashMap<>();

    static {
        registerRenderer(SemiBlockLogistics.class, new SemiBlockRendererLogistics());
        registerRenderer(SemiBlockHeatFrame.class, new SemiBlockRendererHeatFrame());
        registerRenderer(SemiBlockSpawnerAgitator.class, new SemiBlockRendererSpawnerAgitator());
        registerRenderer(SemiBlockCropSupport.class, new SemiBlockRendererCropSupport());
        registerRenderer(SemiBlockTransferGadget.class, new SemiBlockRendererTransferGadget());
    }

    private static <T extends ISemiBlock> void registerRenderer(Class<T> semiBlock, ISemiBlockRenderer<T> renderer) {
        renderers.put(semiBlock, renderer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.player;
        boolean blind = player.isPotionActive(MobEffects.BLINDNESS);

        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        RenderHelper.enableStandardItemLighting();

        for (Map<BlockPos, List<ISemiBlock>> map : SemiBlockManager.getInstance(player.world).getSemiBlocks().values()) {
            for (List<ISemiBlock> semiBlocks : map.values()) {
                for (ISemiBlock semiBlock : semiBlocks) {
                    if (!blind || !(player.getDistanceSq(semiBlock.getPos()) > 25)) {
                        ISemiBlockRenderer renderer = getRenderer(semiBlock);
                        if (renderer != null) {
                            GlStateManager.pushMatrix();
                            GlStateManager.translate(semiBlock.getPos().getX(), semiBlock.getPos().getY(), semiBlock.getPos().getZ());
                            renderer.render(semiBlock, event.getPartialTicks());
                            GlStateManager.popMatrix();
                        }
                    }
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private static ISemiBlockRenderer<?> getRenderer(ISemiBlock semiBlock) {
        Class<?> clazz = semiBlock.getClass();
        while (clazz != Object.class && !renderers.containsKey(clazz)) {
            clazz = clazz.getSuperclass();
        }
        return renderers.get(clazz);
    }
}
