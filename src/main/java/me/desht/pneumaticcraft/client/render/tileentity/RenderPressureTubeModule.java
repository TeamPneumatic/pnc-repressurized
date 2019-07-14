package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

public class RenderPressureTubeModule extends TileEntityRenderer<TileEntityPressureTube> {

    @Override
    public void render(TileEntityPressureTube tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tile.getCamouflage() != null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Hand holdingModule = null;
        if (mc.player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof ItemTubeModule) {
            holdingModule = Hand.MAIN_HAND;
        } else if (mc.player.getHeldItem(Hand.OFF_HAND).getItem() instanceof ItemTubeModule) {
            holdingModule = Hand.OFF_HAND;
        }
        boolean render = false;
        for (int i = 0; i < tile.modules.length; i++) {
            if (tile.modules[i] != null) render = true;
        }
        if (!render && holdingModule == null)
            return;

        GlStateManager.pushMatrix();

        mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.color3f(1, 1, 1);

        GlStateManager.translated((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GlStateManager.scaled(1.0F, -1F, -1F);

        // "fake" module is for showing a preview of where the module would be placed
        if (holdingModule != null) attachFakeModule(mc, tile, holdingModule);

        for (int i = 0; i < tile.modules.length; i++) {
            TubeModule module = tile.modules[i];
            if (module != null) {
                if (module.isFake()) {
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.color4f(1, 1, 1, 0.5f);
                }

                module.getModel().renderModel(0.0625f, module, partialTicks);
                module.doExtraRendering();

                if (module.isFake()) {
                    tile.modules[i] = null;
                    GlStateManager.disableBlend();
                }
                GlStateManager.color4f(1, 1, 1, 1);
            }
        }

        GlStateManager.enableAlphaTest();

        GlStateManager.popMatrix();
    }

    private void attachFakeModule(Minecraft mc, TileEntityPressureTube tile, Hand hand) {
        if (mc.objectMouseOver instanceof BlockRayTraceResult) {
            BlockRayTraceResult brtr = (BlockRayTraceResult) mc.objectMouseOver;
            if (brtr.getPos().equals(tile.getPos()) && mc.world.getTileEntity(brtr.getPos()) == tile) {
                ((BlockPressureTube) ModBlocks.PRESSURE_TUBE).tryPlaceModule(mc.player, mc.world, tile.getPos(), brtr.getFace(), hand,true);
            }
        }
    }
}
