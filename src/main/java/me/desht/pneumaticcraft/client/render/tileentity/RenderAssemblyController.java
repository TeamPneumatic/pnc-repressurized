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
    private static final float TEXT_SIZE = 0.01F;
    private final ModelRenderer screen;

    public RenderAssemblyController(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        screen = new ModelRenderer(64, 64, 33, 32);
        screen.addBox(0F, 0F, 0F, 10, 6, 1);
        screen.setRotationPoint(-5F, 8F, 1F);
        screen.mirror = true;
        screen.rotateAngleX = -0.5934119F;
    }

    @Override
    public void renderModel(TileEntityAssemblyController te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.MODEL_ASSEMBLY_CONTROLLER));

        // have the screen face the player
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180 + Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getYaw()));

        screen.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        // status text
        matrixStackIn.translate(-0.25D, 0.53D, 0.04D);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-34));
        matrixStackIn.scale(TEXT_SIZE, TEXT_SIZE, TEXT_SIZE);
        Minecraft.getInstance().fontRenderer.renderString(te.displayedText, 1, 4, 0xFFFFFFFF, false,  matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);

        // possible problem icon
        if (te.hasProblem) {
            RenderUtils.drawTexture(matrixStackIn, bufferIn.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GUI_PROBLEMS_TEXTURE)), 28, 12, combinedLightIn);
        }
    }
}
