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

package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DroneDigLaserLayer extends RenderLayer<EntityDroneBase, ModelDrone> {
    private static final float LASER_SIZE = 0.4f;

    DroneDigLaserLayer(RenderLayerParent<EntityDroneBase, ModelDrone> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityDroneBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        BlockPos diggingPos = entityIn.getDugBlock();
        if (diggingPos != null) {
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
            matrixStackIn.translate(0, -1, 0);
            BlockState state = entityIn.getCommandSenderWorld().getBlockState(diggingPos);
            VoxelShape shape = state.getShape(entityIn.getCommandSenderWorld(), diggingPos);
            if (shape.isEmpty()) {
                renderLaser(matrixStackIn, bufferIn, partialTicks, entityIn,
                        0, -entityIn.getLaserOffsetY(), 0,
                        diggingPos.getX() + 0.5 - entityIn.getX(), diggingPos.getY() + 0.45 - entityIn.getY(), diggingPos.getZ() + 0.5 - entityIn.getZ());
            } else {
                Vec3 vec = shape.bounds().getCenter().add(Vec3.atLowerCornerOf(diggingPos));
                renderLaser(matrixStackIn, bufferIn, partialTicks, entityIn,
                        0, -entityIn.getLaserOffsetY(), 0,
                        vec.x() - entityIn.getX(), vec.y() - entityIn.getY(), vec.z() - entityIn.getZ());
            }
            matrixStackIn.popPose();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void renderLaser(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks, EntityDroneBase drone, double x1, double y1, double z1, double x2, double y2, double z2) {
        float laserLength = (float) PneumaticCraftUtils.distBetween(x1, y1, z1, x2, y2, z2);

        matrixStack.pushPose();

        matrixStack.translate(x1, y1, z1);

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        float f3 = Mth.sqrt((float) (dx * dx + dz * dz));
        double rotYawRad = Math.atan2(dx, dz);
        double rotPitchRad = Math.PI / 2.0 - Math.atan2(dy, f3);

        matrixStack.mulPose(Vector3f.YP.rotation((float) rotYawRad));
        matrixStack.mulPose(Vector3f.XP.rotation((float) rotPitchRad));

        matrixStack.scale(LASER_SIZE, LASER_SIZE, LASER_SIZE);
        matrixStack.translate(0, 0.6, 0);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(drone.tickCount + partialTicks));

        matrixStack.pushPose();
        matrixStack.scale(1f, laserLength / LASER_SIZE * 2, 1f);

        int[] cols = RenderUtils.decomposeColor(drone.getLaserColor());

        // todo 1.15 consider stitching these 4 into one texture for less state switching
        VertexConsumer builder;

        Matrix4f posMat = matrixStack.last().pose();
        builder = buffer.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.RENDER_LASER));
        renderQuad(posMat, builder, cols);  // glow
        builder = buffer.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.RENDER_LASER_OVERLAY));
        renderQuad(posMat, builder, cols);  // core

        matrixStack.popPose();

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));

        posMat = matrixStack.last().pose();
        builder = buffer.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.RENDER_LASER_START));
        renderQuad(posMat, builder, cols);  // glow
        builder = buffer.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.RENDER_LASER_START_OVERLAY));
        renderQuad(posMat, builder, cols);  // core

        matrixStack.popPose();
    }

    private void renderQuad(Matrix4f posMat, VertexConsumer builder, int[] cols) {
        builder.vertex(posMat,-0.5f, 0f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(0, 0).uv2(RenderUtils.FULL_BRIGHT).endVertex();
        builder.vertex(posMat,-0.5f, 1f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(0, 1).uv2(RenderUtils.FULL_BRIGHT).endVertex();
        builder.vertex(posMat, 0.5f, 1f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(1, 1).uv2(RenderUtils.FULL_BRIGHT).endVertex();
        builder.vertex(posMat, 0.5f, 0f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(1, 0).uv2(RenderUtils.FULL_BRIGHT).endVertex();
    }
}
