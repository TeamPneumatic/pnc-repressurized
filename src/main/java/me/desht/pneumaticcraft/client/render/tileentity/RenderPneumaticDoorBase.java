package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderPneumaticDoorBase extends AbstractTileModelRenderer<TileEntityPneumaticDoorBase> {
    private final ModelRenderer cylinder1;
    private final ModelRenderer cylinder2;
    private final ModelRenderer cylinder3;

    public RenderPneumaticDoorBase(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        cylinder1 = new ModelRenderer(64, 64, 0, 28);
        cylinder1.addBox(0F, 0F, 0F, 3, 3, 10);
        cylinder1.setRotationPoint(2.5F, 8.5F, -6F);
        cylinder1.mirror = true;
        cylinder2 = new ModelRenderer(64, 64, 0, 28);
        cylinder2.addBox(0F, 0F, 0F, 2, 2, 10);
        cylinder2.setRotationPoint(3F, 9F, -6F);
        cylinder2.mirror = true;
        cylinder3 = new ModelRenderer(64, 64, 0, 28);
        cylinder3.addBox(0F, 0F, 0F, 1, 1, 10);
        cylinder3.setRotationPoint(3.5F, 9.5F, -6F);
        cylinder3.mirror = true;
    }

    @Override
    public void renderModel(TileEntityPneumaticDoorBase te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.MODEL_PNEUMATIC_DOOR_BASE));

        float progress = MathHelper.lerp(partialTicks, te.oldProgress, te.progress);

        float cosinus = (float) Math.sin(Math.toRadians((1 - progress) * 90)) * 12 / 16F;
        float sinus = 9 / 16F - (float) Math.cos(Math.toRadians((1 - progress) * 90)) * 9 / 16F;
        double extension = Math.sqrt(Math.pow(sinus, 2) + Math.pow(cosinus + 4 / 16F, 2));

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        matrixStackIn.translate(((te.rightGoing ? -4 : 0) + 2.5) / 16F, 0, -6 / 16F);
        float cylinderAngle = (float) Math.toDegrees(Math.atan(sinus / (cosinus + 14 / 16F)));
        matrixStackIn.rotate(te.rightGoing ? Vector3f.YP.rotationDegrees(cylinderAngle) : Vector3f.YN.rotationDegrees(cylinderAngle));
        matrixStackIn.translate(((te.rightGoing ? -3 : 0) - 2.5) / 16F, 0, 6 / 16F);
        double extensionPart = extension * 0.5D;
        cylinder1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 0, extensionPart);
        cylinder2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 0.8f, 0.8f, 0.8f, 1.0f);
        matrixStackIn.translate(0, 0, extensionPart);
        cylinder3.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 0.6f, 0.6f, 0.6f, 1.0f);
    }
}
