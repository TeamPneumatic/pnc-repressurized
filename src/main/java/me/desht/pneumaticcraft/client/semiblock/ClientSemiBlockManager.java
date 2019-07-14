package me.desht.pneumaticcraft.client.semiblock;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.semiblock.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        boolean blind = player.isPotionActive(Effects.BLINDNESS);

        GlStateManager.pushMatrix();
        GlStateManager.translated(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
        RenderHelper.enableStandardItemLighting();

        for (Map<BlockPos, List<ISemiBlock>> map : SemiBlockManager.getInstance(player.world).getSemiBlocks().values()) {
            for (List<ISemiBlock> semiBlocks : map.values()) {
                for (ISemiBlock semiBlock : semiBlocks) {
                    if (!blind || !(player.getDistanceSq(semiBlock.getCentrePos()) > 25)) {
                        ISemiBlockRenderer renderer = getRenderer(semiBlock);
                        if (renderer != null) {
                            GlStateManager.pushMatrix();
                            GlStateManager.translated(semiBlock.getPos().getX(), semiBlock.getPos().getY(), semiBlock.getPos().getZ());
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
