/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.block.entity.processing.AssemblyDrillBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class AssemblyDrillRenderer extends AbstractAssemblyRenderer<AssemblyDrillBlockEntity> {
    private static final String BASETURN = "baseTurn";
    private static final String BASETURN2 = "baseTurn2";
    private static final String ARMBASE = "armBase";
    private static final String ARMMIDDLE = "armMiddle";
    private static final String DRILLBASE = "drillBase";
    private static final String DRILL = "drill";

    private final ModelPart baseTurn;
    private final ModelPart baseTurn2;
    private final ModelPart armBase;
    private final ModelPart armMiddle;
    private final ModelPart drillBase;
    private final ModelPart drill;

    public AssemblyDrillRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.ASSEMBLY_DRILL);
        baseTurn = root.getChild(BASETURN);
        baseTurn2 = root.getChild(BASETURN2);
        armBase = root.getChild(ARMBASE);
        armMiddle = root.getChild(ARMMIDDLE);
        drillBase = root.getChild(DRILLBASE);
        drill = root.getChild(DRILL);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(BASETURN, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("baseTurn_0", -1.0F, 0.0F, -1.0F, 9, 1, 9, 0, 0)
                        .mirror(),
                PartPose.offset(-3.5F, 22.0F, -3.5F));
        partdefinition.addOrReplaceChild(BASETURN2, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("baseTurn2_0", -2.0F, -0.5F, 0.5F, 2, 6, 3, new CubeDeformation(0.2F), 0, 30)
                        .addBox("baseTurn2_1", -2.0F, 3.75F, -2.0F, 2, 2, 8, 0, 10)
                        .addBox("baseTurn2_2", 4.0F, -0.5F, 0.5F, 2, 6, 3, new CubeDeformation(0.2F), 10, 30)
                        .addBox("baseTurn2_3", 4.0F, 3.75F, -2.0F, 2, 2, 8, 0, 20)
                        .mirror(),
                PartPose.offset(-2.0F, 17.0F, -2.0F));
        partdefinition.addOrReplaceChild(ARMBASE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("armBase_0", 2.0F, 0.0F, 1.0F, 2, 2, 5, new CubeDeformation(0.3F), 0, 49)
                        .addBox("armBase_1", 1.5F, -0.5F, -0.5F, 3, 3, 3, 0, 43)
                        .addBox("armBase_2", 1.5F, -0.5F, 5.5F, 3, 3, 3, 12, 43)
                        .addBox("armBase_3", -1.5F, 0.0F, 0.0F, 9, 2, 2, 0, 39)
                        .mirror(),
                PartPose.offset(-3.0F, 17.0F, -1.0F));
        partdefinition.addOrReplaceChild(ARMMIDDLE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("armMiddle_0", 0.0F, 2.0F, 0.0F, 2, 13, 2, 20, 10)
                        .addBox("armMiddle_1", 0.0F, 0.0F, 0.0F, 2, 2, 2, new CubeDeformation(0.3F), 12, 24)
                        .addBox("armMiddle_2", 0.0F, 15.0F, 0.0F, 2, 2, 2, new CubeDeformation(0.3F), 0, 24)
                        .addBox("armMiddle_3", -0.5F, 15.0F, 0.0F, 3, 2, 2, 14, 52)
                        .mirror(),
                PartPose.offset(-4.0F, 2.0F, 5.0F));
        partdefinition.addOrReplaceChild(DRILLBASE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("drillBase_0", 1.0F, -1.0F, -1.0F, 4, 4, 5, 46, 0)
                        .addBox("drillBase_1", 1.5F, -0.5F, -2.0F, 3, 3, 1, 56, 9)
                        .mirror(),
                PartPose.offset(-3.0F, 2.0F, 4.5F));
        partdefinition.addOrReplaceChild(DRILL, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("drill_0", 2.0F, 0.0F, -2.0F, 1, 1, 4, 50, 9)
                        .mirror(),
                PartPose.offset(-2.5F, 2.5F, 1.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    void renderModel(AssemblyDrillBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float[] angles = new float[5];
        for (int i = 0; i < 4; i++) {
            angles[i] = Mth.lerp(partialTicks, te.oldAngles[i], te.angles[i]);
        }
        angles[4] = Mth.lerp(partialTicks, te.oldDrillRotation, te.drillRotation);

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ASSEMBLY_LASER_AND_DRILL));

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(angles[0]));

        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseTurn2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 18 / 16F, 0);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(angles[1]));
        matrixStackIn.translate(0, -18 / 16F, 0);

        armBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 18 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(angles[2]));
        matrixStackIn.translate(0, -18 / 16F, -6 / 16F);

        armMiddle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 3 / 16F, 6 / 16F);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(angles[3]));
        matrixStackIn.translate(0, -3 / 16F, -6 / 16F);

        drillBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.translate(0, 3 / 16F, 0);
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(angles[4]));
        matrixStackIn.translate(0, -3 / 16F, 0);

        drill.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }
}
