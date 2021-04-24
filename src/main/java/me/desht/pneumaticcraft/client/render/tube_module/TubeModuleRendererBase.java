package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public abstract class TubeModuleRendererBase<T extends TubeModule> {
    private boolean isUpgraded;

    protected final void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public final void renderModule(T module, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        matrixStack.push();

        // transforms to get model orientation right
        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1f, -1f, -1f);

        RenderUtils.rotateMatrixForDirection(matrixStack, module.getDirection());
        isUpgraded = module.isUpgraded();
        float a = module.isFake() ? 0.3f : 1f;

        IVertexBuilder builder = module.isFake() ?
                buffer.getBuffer(RenderType.getEntityTranslucent(getTexture())) :
                buffer.getBuffer(RenderType.getEntityCutout(getTexture()));
        renderDynamic(module, matrixStack, builder, partialTicks, combinedLight, combinedOverlay, 1, 1, 1, a);

        matrixStack.pop();

        renderExtras(module, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
    }

    protected abstract void renderDynamic(T module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a);

    protected abstract ResourceLocation getTexture();

    public void renderExtras(T module, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        // nothing; override in subclasses
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }
}
