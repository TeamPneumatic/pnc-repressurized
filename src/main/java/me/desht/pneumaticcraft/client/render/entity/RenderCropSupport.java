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

package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityCropSupport;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderCropSupport extends RenderSemiblockBase<EntityCropSupport> {
    public static final IRenderFactory<EntityCropSupport> FACTORY = RenderCropSupport::new;

    private final ModelCropSupport model = new ModelCropSupport();

    private RenderCropSupport(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(EntityCropSupport entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entityIn)));
        AxisAlignedBB aabb = entityIn.getBoundingBox();

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, -12/16F, 0);
        if (entityIn.getTimeSinceHit() > 0) {
            wobble(entityIn, partialTicks, matrixStackIn);
        }
        matrixStackIn.scale((float)(aabb.maxX - aabb.minX), 1f, (float)(aabb.maxZ - aabb.minZ));
        model.renderToBuffer(matrixStackIn, builder, packedLightIn, OverlayTexture.pack(0F, false), 0.33f, 0.25f, 0.12f, 1F);

        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCropSupport entity) {
        return Textures.MODEL_HEAT_FRAME;
    }
}
