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
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.PneumaticDoorBaseBlockEntity;
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

public class PneumaticDoorBaseRenderer extends AbstractBlockEntityModelRenderer<PneumaticDoorBaseBlockEntity> {
    private final ModelPart cylinder1;
    private final ModelPart cylinder2;
    private final ModelPart cylinder3;

    private static final String CYLINDER1 = "cylinder1";
    private static final String CYLINDER2 = "cylinder2";
    private static final String CYLINDER3 = "cylinder3";

    public PneumaticDoorBaseRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.PNEUMATIC_DOOR_BASE);
        cylinder1 = root.getChild(CYLINDER1);
        cylinder2 = root.getChild(CYLINDER2);
        cylinder3 = root.getChild(CYLINDER3);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(CYLINDER1, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("cylinder1_0", 0F, 0F, 0F, 3, 3, 10),
                PartPose.offset(2.5F, 8.5F, -6F));
        partdefinition.addOrReplaceChild(CYLINDER2, CubeListBuilder.create().texOffs(0, 13)
                        .addBox("cylinder2_0", 0F, 0F, 0F, 2, 2, 10),
                PartPose.offset(3F, 9F, -6F));
        partdefinition.addOrReplaceChild(CYLINDER3, CubeListBuilder.create().texOffs(0, 25)
                        .addBox("cylinder3_0", 0F, 0F, 0F, 1, 1, 10),
                PartPose.offset(3.5F, 9.5F, -6F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void renderModel(PneumaticDoorBaseBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_PNEUMATIC_DOOR_BASE));

        float progress = Mth.lerp(partialTicks, te.oldProgress, te.progress);

        float cosinus = (float) Math.sin(Math.toRadians((1 - progress) * 90)) * 12 / 16F;
        float sinus = 9 / 16F - (float) Math.cos(Math.toRadians((1 - progress) * 90)) * 9 / 16F;
        double extension = Math.sqrt(Math.pow(sinus, 2) + Math.pow(cosinus + 4 / 16F, 2));

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        matrixStackIn.translate(((te.rightGoing ? -4 : 0) + 2.5) / 16F, 0, -6 / 16F);
        float cylinderAngle = (float) Math.toDegrees(Math.atan(sinus / (cosinus + 14 / 16F)));
        matrixStackIn.mulPose(te.rightGoing ? Axis.YP.rotationDegrees(cylinderAngle) : Axis.YN.rotationDegrees(cylinderAngle));
        matrixStackIn.translate(((te.rightGoing ? -3 : 0) - 2.5) / 16F, 0, 6 / 16F);
        double extensionPart = extension * 0.5D;
        int l = ClientUtils.getLightAt(te.getBlockPos().relative(te.getRotation()));  // avoid cylinders rendering unlit
        cylinder1.render(matrixStackIn, builder, l, combinedOverlayIn);
        matrixStackIn.translate(0, 0, extensionPart);
        cylinder2.render(matrixStackIn, builder, l, combinedOverlayIn, 0xFFCCCCCC);
        matrixStackIn.translate(0, 0, extensionPart);
        cylinder3.render(matrixStackIn, builder, l, combinedOverlayIn, 0xFF989898);
    }
}
