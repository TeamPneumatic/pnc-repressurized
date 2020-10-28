package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.render.tube_module.TubeModuleRendererBase;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;

import java.util.HashMap;
import java.util.Map;

public class RenderPressureTubeModule extends TileEntityRenderer<TileEntityPressureTube> {

    private final Map<ResourceLocation, TubeModuleRendererBase> models = new HashMap<>();

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

        if (tile.tubeModules().noneMatch(e -> true) && holdingModule == null)
            return;

        matrixStack.push();
        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1f, -1f, -1f);

        // "fake" module is for showing a preview of where the module would be placed
        if (holdingModule != null) attachFakeModule(mc, tile, holdingModule);

        for (Direction dir : Direction.VALUES) {
            TubeModule module = tile.getModule(dir);
            if (module != null) {
                // FIXME: map lookup isn't ideal for performance here: need a cached index-based lookup of module->model
                getModel(module).renderModule(module, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);

                if (module.isFake()) {
                    tile.setModule(dir, null);
                }
            }
        }
        matrixStack.pop();
    }

    private TubeModuleRendererBase getModel(TubeModule module) {
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
