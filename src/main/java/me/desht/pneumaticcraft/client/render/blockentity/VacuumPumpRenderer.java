package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.VacuumPumpBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class VacuumPumpRenderer extends AbstractBlockEntityModelRenderer<VacuumPumpBlockEntity> {
    private static final int BLADE_COUNT = 6;

    private static final String BLADE = "blade";

    private final ModelPart blade;

    public VacuumPumpRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.VACUUM_PUMP);
        blade = root.getChild(BLADE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(BLADE, CubeListBuilder.create().texOffs(24, 25)
                        .addBox("blade_0", 0.0F, 0.0F, -1.0F, 1, 4, 3),
                PartPose.offset(-0.5F, 14.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderModel(VacuumPumpBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityTranslucent(Textures.MODEL_VACUUM_PUMP));

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(-90));

        renderBlades(te, partialTicks, matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }


    private void renderBlades(VacuumPumpBlockEntity te, float partialTicks, PoseStack matrixStackIn, VertexConsumer builder, int combinedLightIn, int combinedOverlayIn) {
        float rotation = Mth.lerp(partialTicks, te.oldRotation, te.rotation) + 1;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0, -0.68f, 1f);
        matrixStackIn.scale(0.8f, 0.8f, 0.8f);
        for (int i = 0; i < BLADE_COUNT; i++) {
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(rotation * 2 + (i + 0.5F) / BLADE_COUNT * 360));
            matrixStackIn.translate(0, 0, 1D / 16D);
            blade.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180));
    }
}
