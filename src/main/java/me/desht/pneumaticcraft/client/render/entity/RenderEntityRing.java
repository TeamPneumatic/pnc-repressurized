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
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderEntityRing extends EntityRenderer<EntityRing> {

    public static final IRenderFactory<EntityRing> FACTORY = RenderEntityRing::new;

    private RenderEntityRing(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(EntityRing ring, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (ring.oldRing != null) {
            float yaw = MathHelper.lerp(partialTicks, ring.yRotO, ring.yRot);
            float pitch = MathHelper.lerp(partialTicks, ring.xRotO, ring.xRot);
            RenderUtils.renderRing(ring.ring, ring.oldRing, matrixStackIn, bufferIn, partialTicks, yaw, pitch, ring.color);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRing entity) {
        return null;
    }
}
