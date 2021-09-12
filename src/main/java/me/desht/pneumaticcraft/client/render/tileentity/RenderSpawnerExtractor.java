package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySpawnerExtractor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderSpawnerExtractor extends AbstractTileModelRenderer<TileEntitySpawnerExtractor> {
    private final ModelRenderer model;

    public RenderSpawnerExtractor(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        model = new ModelRenderer(64, 32, 0, 0);
        model.setPos(0.0F, 24.0F, 0.0F);
        model.texOffs(0, 15).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 15.0F, 2.0F, 0.0F, false);
        model.texOffs(0, 0).addBox(-2.0F, -19.0F, -2.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);
        model.texOffs(0, 11).addBox(-7.0F, -18.0F, -1.0F, 14.0F, 2.0F, 2.0F, 0.0F, false);
        model.texOffs(16, 0).addBox(-2.0F, -19.0F, -6.0F, 4.0F, 4.0F, 2.0F, 0.0F, false);
        model.texOffs(16, 0).addBox(-2.0F, -19.0F, 4.0F, 4.0F, 4.0F, 2.0F, 0.0F, false);
        model.texOffs(28, 0).addBox(4.0F, -19.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.0F, false);
        model.texOffs(28, 0).addBox(-6.0F, -19.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.0F, false);
        model.texOffs(8, 16).addBox(-1.0F, -18.0F, -7.0F, 2.0F, 2.0F, 14.0F, 0.0F, false);
        model.texOffs(40, 0).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
    }

    @Override
    void renderModel(TileEntitySpawnerExtractor te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_SPAWNER_EXTRACTOR));

        float extension = te.getProgress() * -0.75f;
        float rot = MathHelper.lerp(te.getMode() == TileEntitySpawnerExtractor.Mode.FINISHED ? 0f : partialTicks, te.getPrevRotationDegrees(), te.getRotationDegrees());

        matrixStackIn.translate(0, extension, 0);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rot));
        model.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
