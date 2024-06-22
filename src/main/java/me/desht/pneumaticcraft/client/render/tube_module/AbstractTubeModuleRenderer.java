package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractTubeModuleRenderer<T extends AbstractTubeModule> {
    public final void renderModule(T module, PoseStack matrixStack, MultiBufferSource buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        matrixStack.pushPose();

        // transforms to get model orientation right
        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1f, -1f, -1f);

        RenderUtils.rotateMatrixForDirection(matrixStack, module.getDirection());
        int alpha = module.isFake() ? 80 : 255;

        VertexConsumer builder = module.isFake() ?
                buffer.getBuffer(RenderType.entityTranslucent(getTexture(module.isUpgraded()))) :
                buffer.getBuffer(RenderType.entityCutout(getTexture(module.isUpgraded())));
        render(module, matrixStack, builder, partialTicks, combinedLight, combinedOverlay, alpha);

        matrixStack.popPose();

        renderExtras(module, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
    }

    protected abstract void render(T module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, int alpha);

    protected abstract ResourceLocation getTexture(boolean isUpgraded);

    public void renderExtras(T module, PoseStack matrixStack, MultiBufferSource buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        // nothing; override in subclasses
    }
}
