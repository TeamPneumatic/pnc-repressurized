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

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.RingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderEntityRing extends EntityRenderer<RingEntity> {
    public RenderEntityRing(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(RingEntity ring, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (ring.oldRing != null) {
            float yaw = Mth.lerp(partialTicks, ring.yRotO, ring.getYRot());
            float pitch = Mth.lerp(partialTicks, ring.xRotO, ring.getXRot());
            RenderUtils.renderRing(ring.ring, ring.oldRing, matrixStackIn, bufferIn, partialTicks, yaw, pitch, ring.color);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(RingEntity entity) {
        return null;
    }
}
