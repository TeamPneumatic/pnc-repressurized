package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

public class RenderAssemblyController extends AbstractTileModelRenderer<TileEntityAssemblyController> {
    private static final float TEXT_SIZE = 0.007F;
    private final ModelRenderer screen;

    public RenderAssemblyController(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        screen = new ModelRenderer(64, 64, 0, 0);
        screen.setPos(-5.0F, 8.0F, 1.0F);
        screen.texOffs(16, 0).addBox(-1.0F, 0.0F, -1.0F, 12.0F, 6.0F, 2.0F, 0.0F, true);
        screen.xRot = -0.5934119F;
    }

    @Override
    public void renderModel(TileEntityAssemblyController te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ASSEMBLY_CONTROLLER));

        // have the screen face the player
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180 + Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));

        screen.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        // status text
        matrixStackIn.translate(-0.23D, 0.50D, -0.04D);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-34));
        matrixStackIn.scale(TEXT_SIZE, TEXT_SIZE, TEXT_SIZE);
        Minecraft.getInstance().font.drawInBatch(te.displayedText, 1, 4, 0xFF4ce568, false,  matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);

        // possible problem icon
        if (te.hasProblem) {
            RenderUtils.drawTexture(matrixStackIn, bufferIn.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GUI_GREEN_PROBLEMS_TEXTURE)), 0, 18, combinedLightIn);
        }
    }
}
