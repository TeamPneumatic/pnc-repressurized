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
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.block.entity.elevator.ElevatorBaseBlockEntity;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ElevatorBaseRenderer extends AbstractBlockEntityModelRenderer<ElevatorBaseBlockEntity> {
    private static final float FACTOR = 9F / 16;
    private static final int[] SHADE = new int[] { 0xFFFFFFFF, 0xFFD8D8D8, 0xFFC0C0C0, 0xFF8C8C8C };

    private final ModelPart pole1;
    private final ModelPart pole2;
    private final ModelPart pole3;
    private final ModelPart pole4;
    private final ModelPart floor;

    private static final String POLE1 = "pole1";
    private static final String POLE2 = "pole2";
    private static final String POLE3 = "pole3";
    private static final String POLE4 = "pole4";
    private static final String FLOOR = "floor";

    public ElevatorBaseRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.ELEVATOR_BASE);
        this.pole1 = root.getChild(POLE1);
        this.pole2 = root.getChild(POLE2);
        this.pole3 = root.getChild(POLE3);
        this.pole4 = root.getChild(POLE4);
        this.floor = root.getChild(FLOOR);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(POLE1, CubeListBuilder.create().texOffs(28, 41)
                        .addBox("pole1_0", -19.5F, 0.0F, -1.5F, 5, 14, 5),
                PartPose.offset(17.0F, 9.0F, -1.0F));
        partdefinition.addOrReplaceChild(POLE2, CubeListBuilder.create().texOffs(32, 19)
                        .addBox("pole2_0", -15.0F, 0.0F, -1.0F, 6, 14, 6),
                PartPose.offset(12.0F, 9.0F, -2.0F));
        partdefinition.addOrReplaceChild(POLE3, CubeListBuilder.create().texOffs(0, 39)
                        .addBox("pole3_0", -8.5F, 0.0F, 8.5F, 7, 14, 7),
                PartPose.offset(5.0F, 9.0F, -12.0F));
        partdefinition.addOrReplaceChild(POLE4, CubeListBuilder.create().texOffs(0, 17)
                        .addBox("pole4_0", 0.0F, 0.0F, 0.0F, 8, 14, 8),
                PartPose.offset(-4.0F, 9.0F, -4.0F));
        partdefinition.addOrReplaceChild(FLOOR, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("floor_0", 0.0F, 0.0F, 0.0F, 16, 1, 16),
                PartPose.offset(-8.0F, 8.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void renderModel(ElevatorBaseBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.extension == 0) return;

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ELEVATOR));

        double extension = Mth.lerp(partialTicks, te.oldExtension, te.extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole4, 0, extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole3, 1, extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole2, 2, extension);
        renderPole(matrixStackIn, builder, te.lightAbove, combinedOverlayIn, pole1, 3, extension);

        floor.render(matrixStackIn, builder, te.lightAbove, combinedOverlayIn);
    }

    @Override
    protected void renderExtras(ElevatorBaseBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.fakeFloorTextureUV != null && te.fakeFloorTextureUV.length == 4) {
            matrixStack.pushPose();
            double extension = Mth.lerp(partialTicks, te.oldExtension, te.extension);
            matrixStack.translate(0, extension + 1.0005f, 0);
            VertexConsumer builder = iRenderTypeBuffer.getBuffer(ModRenderTypes.getTextureRender(TextureAtlas.LOCATION_BLOCKS));
            float uMin = te.fakeFloorTextureUV[0];
            float vMin = te.fakeFloorTextureUV[1];
            float uMax = te.fakeFloorTextureUV[2];
            float vMax = te.fakeFloorTextureUV[3];
            Matrix4f posMat = matrixStack.last().pose();
            builder.addVertex(posMat,0, 0, 1).setColor(te.fakeFloorTextureTint)
                    .setUv(uMin, vMax).setLight(te.lightAbove);
            builder.addVertex(posMat,1, 0, 1).setColor(te.fakeFloorTextureTint)
                    .setUv(uMax, vMax).setLight(te.lightAbove);
            builder.addVertex(posMat,1, 0, 0).setColor(te.fakeFloorTextureTint)
                    .setUv(uMax, vMin).setLight(te.lightAbove);
            builder.addVertex(posMat,0, 0, 0).setColor(te.fakeFloorTextureTint)
                    .setUv(uMin, vMin).setLight(te.lightAbove);
            matrixStack.popPose();
        }
    }

    private void renderPole(PoseStack matrixStackIn, VertexConsumer builder, int combinedLightIn, int combinedOverlayIn, ModelPart pole, int idx, double extension) {
        matrixStackIn.translate(0, -extension / 4, 0);
        matrixStackIn.pushPose();
        matrixStackIn.translate(0, FACTOR, 0);
        matrixStackIn.scale(1, (float) (extension * 16 / 14 / 4), 1);
        matrixStackIn.translate(0, -FACTOR, 0);
        pole.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, SHADE[idx]);
        matrixStackIn.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ElevatorBaseBlockEntity te) {
        return true;  // since this can get very tall
    }

    @Override
    public boolean shouldRender(ElevatorBaseBlockEntity pBlockEntity, Vec3 pCameraPos) {
        // ignore camera Y pos for purposes of render visibility (like the beacon does)
        return Vec3.atCenterOf(pBlockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D)
                .closerThan(pCameraPos.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }

    @Override
    public AABB getRenderBoundingBox(ElevatorBaseBlockEntity blockEntity) {
        return super.getRenderBoundingBox(blockEntity).inflate(blockEntity.extension);
    }
}
