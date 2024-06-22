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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class RenderCoordWireframe {
    public final BlockPos pos;
    public final ResourceKey<Level> worldKey;
    public int ticksExisted;

    public RenderCoordWireframe(Level world, BlockPos pos) {
        this.worldKey = world.dimension();
        this.pos = pos;
    }

    public void render(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        float minX = 0;
        float minY = 0;
        float minZ = 0;
        float maxX = 1;
        float maxY = 1;
        float maxZ = 1;
        float progress = (ticksExisted % 20 + partialTicks) / 20;
        matrixStack.pushPose();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        float g = progress < 0.5F ? progress + 0.5F : 1.5F - progress;
        RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.BLOCK_TRACKER, (posMat, builder) -> {
            builder.addVertex(posMat, minX, minY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, minX, maxY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, minX, minY, maxZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, minX, maxY, maxZ).setColor(0, g, 1, 1);

            builder.addVertex(posMat, maxX, minY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, maxY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, minY, maxZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, maxY, maxZ).setColor(0, g, 1, 1);

            builder.addVertex(posMat, minX, minY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, minY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, minX, minY, maxZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, minY, maxZ).setColor(0, g, 1, 1);

            builder.addVertex(posMat, minX, maxY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, maxY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, minX, maxY, maxZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, maxY, maxZ).setColor(0, g, 1, 1);

            builder.addVertex(posMat, minX, minY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, minX, minY, maxZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, minY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, minY, maxZ).setColor(0, g, 1, 1);

            builder.addVertex(posMat, minX, maxY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, minX, maxY, maxZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, maxY, minZ).setColor(0, g, 1, 1);
            builder.addVertex(posMat, maxX, maxY, maxZ).setColor(0, g, 1, 1);
        });

        matrixStack.popPose();
    }
}
