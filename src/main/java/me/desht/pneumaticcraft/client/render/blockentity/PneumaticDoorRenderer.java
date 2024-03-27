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
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.PneumaticDoorBlock;
import me.desht.pneumaticcraft.common.block.entity.PneumaticDoorBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;

public class PneumaticDoorRenderer extends AbstractBlockEntityModelRenderer<PneumaticDoorBlockEntity> {
    private static final String DOOR = "door";

    private final ModelPart door;

    public PneumaticDoorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.PNEUMATIC_DOOR);
        door = root.getChild(DOOR);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(DOOR, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("door_0", 0.0F, -11.0F, 0.0F, 16, 32, 3, 0, 0)
                        .addBox("door_1", 1.0F, -11.0F, 2.25F, 1, 32, 1, new CubeDeformation(-0.01F), 42, 2)
                        .addBox("door_2", 3.0F, -11.0F, 2.25F, 1, 32, 1, new CubeDeformation(-0.01F), 38, 2)
                        .addBox("door_3", 3.0F, -11.0F, -0.25F, 1, 32, 1, new CubeDeformation(-0.01F), 38, 2)
                        .addBox("door_4", 1.0F, -11.0F, -0.25F, 1, 32, 1, new CubeDeformation(-0.01F), 42, 2)
                        .addBox("door_5", 0.0F, -9.0F, 2.5F, 5, 1, 1, new CubeDeformation(-0.01F), 0, 46)
                        .addBox("door_6", 0.0F, 18.0F, 2.5F, 5, 1, 1, new CubeDeformation(-0.01F), 0, 44)
                        .addBox("door_7", 0.0F, -9.0F, -0.5F, 5, 1, 1, new CubeDeformation(-0.01F), 0, 46)
                        .addBox("door_8", 0.0F, 18.0F, -0.5F, 5, 1, 1, new CubeDeformation(-0.01F), 0, 44)
                        .addBox("door_9", 0.5F, 1.0F, 3.0F, 4, 8, 1, 16, 35)
                        .addBox("door_10", 0.5F, 1.0F, -1.0F, 4, 8, 1, 16, 35)
                        .addBox("door_11", 1.5F, 2.0F, 4.0F, 2, 2, 1, 26, 35)
                        .addBox("door_12", 1.5F, 2.0F, -2.0F, 2, 2, 1, 26, 35)
                        .addBox("door_13", 2.5F, 2.5F, 4.0F, 4, 1, 1, new CubeDeformation(-0.2F), 26, 38)
                        .addBox("door_14", 2.5F, 2.5F, -2.0F, 4, 1, 1, new CubeDeformation(-0.2F), 26, 38)
                        .addBox("door_15", 9.0F, -8.0F, 2.25F, 7, 2, 1, new CubeDeformation(-0.01F), 0, 41)
                        .addBox("door_16", 9.0F, 9.0F, 2.25F, 7, 2, 1, new CubeDeformation(-0.01F), 0, 38)
                        .addBox("door_17", 9.0F, 16.0F, 2.25F, 7, 2, 1, new CubeDeformation(-0.01F), 0, 35)
                        .addBox("door_18", 9.0F, -8.0F, -0.25F, 7, 2, 1, new CubeDeformation(-0.01F), 0, 41)
                        .addBox("door_19", 9.0F, 9.0F, -0.25F, 7, 2, 1, new CubeDeformation(-0.01F), 0, 38)
                        .addBox("door_20", 9.0F, 16.0F, -0.25F, 7, 2, 1, new CubeDeformation(-0.01F), 0, 35)
                        .mirror(),
                PartPose.offset(-8.0F, 3.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderModel(PneumaticDoorBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.getBlockState().getValue(PneumaticDoorBlock.TOP_DOOR)) return;

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_PNEUMATIC_DOOR_DYNAMIC));

        float rotation = Mth.lerp(partialTicks, te.oldRotationAngle, te.rotationAngle);
        boolean rightGoing = te.rightGoing;
        float[] rgb = DyeColor.byId(te.color).getTextureDiffuseColors();

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        matrixStackIn.translate((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
        matrixStackIn.mulPose(rightGoing ? Axis.YN.rotationDegrees(rotation) : Axis.YP.rotationDegrees(rotation));
        matrixStackIn.translate((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);

        if (rightGoing) {
            matrixStackIn.translate(0, 0, -6.5/16F);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(180));
            matrixStackIn.translate(0, 0, 6.5/16F);
        }
        door.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
    }

    @Override
    public AABB getRenderBoundingBox(PneumaticDoorBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
