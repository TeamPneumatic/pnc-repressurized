package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.AirCannonBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AirCannonRenderer extends AbstractBlockEntityModelRenderer<AirCannonBlockEntity> {
    private final ModelPart baseTurn;
    private final ModelPart baseFrame1;
    private final ModelPart baseFrame2;
    private final ModelPart axis;
    private final ModelPart cannon;

    private static final String BASETURN = "baseTurn";
    private static final String BASEFRAME1 = "baseFrame1";
    private static final String BASEFRAME2 = "baseFrame2";
    private static final String AXIS = "axis";
    private static final String CANNON = "cannon";

    public AirCannonRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.AIR_CANNON);
        baseTurn = root.getChild(BASETURN);
        baseFrame1 = root.getChild(BASEFRAME1);
        baseFrame2 = root.getChild(BASEFRAME2);
        axis = root.getChild(AXIS);
        cannon = root.getChild(CANNON);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(BASETURN, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("baseTurn_0", 0.0F, 0.0F, 0.0F, 7, 1, 7, 0, 0),
                PartPose.offset(-3.5F, 20.0F, -5.0F));
        partdefinition.addOrReplaceChild(BASEFRAME1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("baseFrame1_0", 0.0F, 0.0F, 0.0F, 1, 5, 3, 28, 0)
                        .mirror(),
                PartPose.offset(-3.5F, 15.0F, -3.0F));
        partdefinition.addOrReplaceChild(BASEFRAME2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("baseFrame2_0", 0.0F, 0.0F, 0.0F, 1, 5, 3, 36, 0)
                        .mirror(),
                PartPose.offset(2.5F, 15.0F, -3.0F));
        partdefinition.addOrReplaceChild(AXIS, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("axis_0", -1.0F, 0.0F, -0.5F, 8, 2, 2, new CubeDeformation(-0.2F), 44, 4)
                        .mirror(),
                PartPose.offset(-3.0F, 15.5F, -2.0F));
        partdefinition.addOrReplaceChild(CANNON, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("cannon_0", -1.0F, 0.0F, -1.0F, 4, 4, 4, 0, 8)
                        .addBox("cannon_1", -0.5F, -2.0F, -0.5F, 3, 2, 3, 24, 8)
                        .addBox("cannon_2", -1.0F, -3.75F, -0.5F, 1, 2, 3, new CubeDeformation(-0.2F), 36, 8)
                        .addBox("cannon_3", 2.0F, -3.75F, -0.5F, 1, 2, 3, new CubeDeformation(-0.2F), 44, 8)
                        .addBox("cannon_4", -1.0F, -3.75F, -1.0F, 4, 2, 1, new CubeDeformation(-0.2F), 44, 13)
                        .addBox("cannon_5", -1.0F, -3.75F, 2.0F, 4, 2, 1, new CubeDeformation(-0.2F), 34, 13)
                        .mirror(),
                PartPose.offset(-1.0F, 15.0F, -2.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    void renderModel(AirCannonBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_AIR_CANNON));

        float angle = RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        float rotationAngle = te.rotationAngle - angle + 180F;

        matrixStackIn.translate(0.0, 0.0, -0.09375D);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(rotationAngle));
        matrixStackIn.translate(0.0, 0.0, 0.09375D);
        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseFrame2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        axis.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0.0D, 1.0D, -0.09375D);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(te.heightAngle));
        matrixStackIn.translate(0.0D, -1.0D, 0.09375D);
        cannon.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
