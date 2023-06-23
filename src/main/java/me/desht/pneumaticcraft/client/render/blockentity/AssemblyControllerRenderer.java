package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.AssemblyControllerBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AssemblyControllerRenderer extends AbstractBlockEntityModelRenderer<AssemblyControllerBlockEntity> {
    private static final float TEXT_SIZE = 0.007F;
    private final ModelPart screen;

    private static final String SCREEN = "screen";

    public AssemblyControllerRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.ASSEMBLY_CONTROLLER);
        screen = root.getChild(SCREEN);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(SCREEN, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("screen_0", -1.0F, 0.0F, -1.0F, 12, 6, 2, 16, 0)
                        .mirror(),
                PartPose.offsetAndRotation(-5.0F, 8.0F, 1.0F, -0.5934119F, 0, 0));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderModel(AssemblyControllerBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ASSEMBLY_CONTROLLER));

        // have the screen face the player
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180 + Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));

        screen.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        // status text
        matrixStackIn.translate(-0.23D, 0.50D, -0.04D);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(-34));
        matrixStackIn.scale(TEXT_SIZE, TEXT_SIZE, TEXT_SIZE);
        Minecraft.getInstance().font.drawInBatch("> " + te.displayedText, 1, 4, 0xFF4ce568, false,  matrixStackIn.last().pose(), bufferIn, Font.DisplayMode.NORMAL, 0, combinedLightIn);

        // possible problem icon
        if (te.hasProblem) {
            RenderUtils.drawTexture(matrixStackIn, bufferIn.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GUI_GREEN_PROBLEMS_TEXTURE)), 0, 18, combinedLightIn);
        }
    }
}
