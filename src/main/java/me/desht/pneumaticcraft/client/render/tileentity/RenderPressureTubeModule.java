package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.model.module.AbstractModelRenderer;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;

import java.util.HashMap;
import java.util.Map;

public class RenderPressureTubeModule extends TileEntityRenderer<TileEntityPressureTube> {

    private final Map<ResourceLocation, AbstractModelRenderer> models = new HashMap<>();

    public RenderPressureTubeModule(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityPressureTube tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
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

        matrixStack.push();

//        mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//        GlStateManager.enableTexture();
//        GlStateManager.disableAlphaTest();
//        GlStateManager.color3f(1, 1, 1);

//        GlStateManager.translated((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
//        GlStateManager.scaled(1.0F, -1F, -1F);

        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1f, -1f, -1f);

        // "fake" module is for showing a preview of where the module would be placed
        if (holdingModule != null) attachFakeModule(mc, tile, holdingModule);

        for (int i = 0; i < tile.modules.length; i++) {
            TubeModule module = tile.modules[i];
            if (module != null) {
//                if (module.isFake()) {
//                    GlStateManager.enableBlend();
//                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//                    GlStateManager.color4f(1, 1, 1, 0.5f);
//                }

                // FIXME: map lookup isn't ideal for performance here: need a cached index-based lookup of module->model
                getModel(module).renderModule(module, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);

                if (module.isFake()) {
                    tile.modules[i] = null;
//                    GlStateManager.disableBlend();
                }
//                GlStateManager.color4f(1, 1, 1, 1);
            }
        }

//        GlStateManager.enableAlphaTest();

        matrixStack.pop();
//        GlStateManager.popMatrix();
    }

    private AbstractModelRenderer getModel(TubeModule module) {
        return models.computeIfAbsent(module.getType(), k -> TubeModuleClientRegistry.createModel(module));
    }

    private void attachFakeModule(Minecraft mc, TileEntityPressureTube tile, Hand hand) {
        if (mc.objectMouseOver instanceof BlockRayTraceResult) {
            BlockRayTraceResult brtr = (BlockRayTraceResult) mc.objectMouseOver;
            if (brtr.getPos().equals(tile.getPos()) && mc.world.getTileEntity(brtr.getPos()) == tile) {
                ModBlocks.PRESSURE_TUBE.get().tryPlaceModule(mc.player, mc.world, tile.getPos(), brtr.getFace(), hand,true);
            }
        }
    }
}
