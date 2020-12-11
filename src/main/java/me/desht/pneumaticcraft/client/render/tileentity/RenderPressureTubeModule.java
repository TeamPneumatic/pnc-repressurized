package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.render.tube_module.TubeModuleRendererBase;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<TubeModule> modules = tile.tubeModules().collect(Collectors.toList());

        if (modules.isEmpty() && holdingModule == null)
            return;

        if (holdingModule != null && mc.objectMouseOver instanceof BlockRayTraceResult) {
            // "fake" module is for showing a preview of where the module would be placed
            BlockRayTraceResult brtr = (BlockRayTraceResult) mc.objectMouseOver;
            if (brtr.getPos().equals(tile.getPos()) && mc.world.getTileEntity(brtr.getPos()) == tile && tile.getModule(brtr.getFace()) == null) {
                TubeModule fakeModule = ((ItemTubeModule) mc.player.getHeldItem(holdingModule).getItem()).createModule();
                fakeModule.markFake();
                fakeModule.setDirection(brtr.getFace());
                fakeModule.setTube(tile);
                getModuleRenderer(fakeModule).renderModule(fakeModule, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
            }
        }

        for (TubeModule m : modules) {
            getModuleRenderer(m).renderModule(m, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
            if (m.isFake()) tile.setModule(m.getDirection(), null);
        }
    }

    private TubeModuleRendererBase getModuleRenderer(TubeModule module) {
        return models.computeIfAbsent(module.getType(), k -> TubeModuleClientRegistry.createModel(module));
    }

    @Override
    public boolean isGlobalRenderer(TileEntityPressureTube te) {
        return te.tubeModules().findAny().isPresent();
    }
}
