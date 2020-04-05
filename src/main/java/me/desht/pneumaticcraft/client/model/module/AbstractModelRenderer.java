package me.desht.pneumaticcraft.client.model.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractModelRenderer<T extends TubeModule> {

    protected final void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public final void renderModule(T module, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        matrixStack.push();

        RenderUtils.rotateMatrixForDirection(matrixStack, module.getDirection());
        float r, g, b, a;
        if (module.isUpgraded()) {
            r = 0.75f;
            g = 1f;
            b = 0.4f;
            a = 1f;
        } else {
            r = g = b = a = 1f;
        }
        if (module.isFake()) a = 0.5f;

        IVertexBuilder builder = module.isFake() ?
                buffer.getBuffer(RenderType.getEntityTranslucent(getTexture())) :
                buffer.getBuffer(RenderType.getEntityCutout(getTexture()));
        renderDynamic(module, matrixStack, builder, partialTicks, combinedLight, combinedOverlay, r, g, b, a);

        matrixStack.pop();

        renderExtras(module, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
    }

    protected abstract void renderDynamic(T module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a);

    protected abstract ResourceLocation getTexture();

    protected void renderExtras(T module,  MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLight, int combinedOverlay) {
        // nothing; override in subclasses
    }
}
