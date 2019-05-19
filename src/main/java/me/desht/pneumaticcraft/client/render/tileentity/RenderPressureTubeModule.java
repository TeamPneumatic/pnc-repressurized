package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public class RenderPressureTubeModule extends TileEntitySpecialRenderer<TileEntityPressureTube> {

    @Override
    public void render(TileEntityPressureTube tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile.getCamouflage() != null) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EnumHand holdingModule = null;
        if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemTubeModule) {
            holdingModule = EnumHand.MAIN_HAND;
        } else if (mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemTubeModule) {
            holdingModule = EnumHand.OFF_HAND;
        }
        boolean render = false;
        for (int i = 0; i < tile.modules.length; i++) {
            if (tile.modules[i] != null) render = true;
        }
        if (!render && holdingModule == null)
            return;

        GlStateManager.pushMatrix();

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.color(1, 1, 1);

        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GlStateManager.scale(1.0F, -1F, -1F);

        // "fake" module is for showing a preview of where the module would be placed
        if (holdingModule != null) attachFakeModule(mc, tile, holdingModule);

        for (int i = 0; i < tile.modules.length; i++) {
            TubeModule module = tile.modules[i];
            if (module != null) {
                if (module.isFake()) {
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.color(1, 1, 1, 0.5f);
                }

                module.getModel().renderModel(0.0625f, module, partialTicks);
                module.doExtraRendering();

                if (module.isFake()) {
                    tile.modules[i] = null;
                    GlStateManager.disableBlend();
                }
                GlStateManager.color(1, 1, 1, 1);
            }
        }

        GlStateManager.enableAlpha();

        GlStateManager.popMatrix();
    }

    private void attachFakeModule(Minecraft mc, TileEntityPressureTube tile, EnumHand hand) {
        RayTraceResult rtr = mc.objectMouseOver;
        if (rtr != null
                && rtr.typeOfHit == RayTraceResult.Type.BLOCK
                && rtr.getBlockPos().equals(tile.getPos())
                && mc.world.getTileEntity(rtr.getBlockPos()) == tile) {
            ((BlockPressureTube) Blockss.PRESSURE_TUBE).tryPlaceModule(mc.player, mc.world, tile.getPos(), rtr.sideHit, hand , true);
        }
    }
}
