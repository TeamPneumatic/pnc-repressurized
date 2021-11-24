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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class DroneDigLaserLayer extends LayerRenderer<EntityDroneBase, ModelDrone> {
    private static final float LASER_SIZE = 0.4f;

    DroneDigLaserLayer(IEntityRenderer<EntityDroneBase, ModelDrone> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityDroneBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
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
                Vector3d vec = shape.bounds().getCenter().add(Vector3d.atLowerCornerOf(diggingPos));
                renderLaser(matrixStackIn, bufferIn, partialTicks, entityIn,
                        0, -entityIn.getLaserOffsetY(), 0,
                        vec.x() - entityIn.getX(), vec.y() - entityIn.getY(), vec.z() - entityIn.getZ());
            }
            matrixStackIn.popPose();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void renderLaser(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, EntityDroneBase drone, double x1, double y1, double z1, double x2, double y2, double z2) {
        float laserLength = (float) PneumaticCraftUtils.distBetween(x1, y1, z1, x2, y2, z2);

        matrixStack.pushPose();

        matrixStack.translate(x1, y1, z1);

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        float f3 = MathHelper.sqrt(dx * dx + dz * dz);
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
        IVertexBuilder builder;

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

    private void renderQuad(Matrix4f posMat, IVertexBuilder builder, int[] cols) {
        builder.vertex(posMat,-0.5f, 0f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(0, 0).uv2(RenderUtils.FULL_BRIGHT).endVertex();
        builder.vertex(posMat,-0.5f, 1f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(0, 1).uv2(RenderUtils.FULL_BRIGHT).endVertex();
        builder.vertex(posMat, 0.5f, 1f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(1, 1).uv2(RenderUtils.FULL_BRIGHT).endVertex();
        builder.vertex(posMat, 0.5f, 0f, 0f).color(cols[1], cols[2], cols[3], cols[0]).uv(1, 0).uv2(RenderUtils.FULL_BRIGHT).endVertex();
    }
}
