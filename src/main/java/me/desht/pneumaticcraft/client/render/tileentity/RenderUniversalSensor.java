package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderUniversalSensor extends AbstractTileModelRenderer<TileEntityUniversalSensor> {
    private final ModelRenderer dish1;
    private final ModelRenderer dish2;
    private final ModelRenderer dish3;
    private final ModelRenderer dish4;
    private final ModelRenderer dish5;
    private final ModelRenderer dish6;

    public RenderUniversalSensor(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        dish1 = new ModelRenderer(64, 64, 0, 33);
        dish1.addBox(-2F, 0F, -2F, 4, 1, 4);
        dish1.setPos(0F, 16F, 0F);
        dish1.mirror = true;

        dish2 = new ModelRenderer(64, 64, 0, 38);
        dish2.addBox(-3F, -1F, 0F, 1, 8, 4);
        dish2.setPos(0F, 9F, -2F);
        dish2.mirror = true;
        dish2.zRot = -0.2268928F;

        dish3 = new ModelRenderer(64, 64, 0, 50);
        dish3.addBox(-3.8F, 0F, 0.8F, 1, 4, 4);
        dish3.setPos(0F, 8F, 0F);
        dish3.mirror = true;
        dish3.xRot = 0.0698132F;
        dish3.yRot = 0.3839724F;
        dish3.zRot = -0.2268928F;

        dish4 = new ModelRenderer(64, 64, 10, 50);
        dish4.addBox(-3.8F, 0F, -4.7F, 1, 4, 4);
        dish4.setPos(0F, 8F, 0F);
        dish4.mirror = true;
        dish4.xRot = -0.0698132F;
        dish4.yRot = -0.3839724F;
        dish4.zRot = -0.2268928F;

        dish5 = new ModelRenderer(64, 64, 0, 58);
        dish5.addBox(-2F, 0F, -0.5F, 6, 1, 1);
        dish5.setPos(0F, 12F, 0F);
        dish5.mirror = true;
        dish5.zRot = -0.2268928F;

        dish6 = new ModelRenderer(64, 64, 0, 60);
        dish6.addBox(3F, 0F, -1F, 1, 1, 2);
        dish6.setPos(0F, 10.2F, 0F);
        dish6.mirror = true;
    }

    @Override
    public void renderModel(TileEntityUniversalSensor te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_UNIVERSAL_SENSOR));

        float rotation = MathHelper.lerp(partialTicks, te.oldDishRotation, te.dishRotation);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotation));
        dish1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        dish2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        dish3.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        dish4.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        dish5.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        dish6.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
