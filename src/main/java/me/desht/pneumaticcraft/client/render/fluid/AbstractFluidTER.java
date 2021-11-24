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

package me.desht.pneumaticcraft.client.render.fluid;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Collection;

public abstract class AbstractFluidTER<T extends TileEntityBase> extends TileEntityRenderer<T> {
    AbstractFluidTER(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(T te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.getLevel().getChunkSource().isEntityTickingChunk(new ChunkPos(te.getBlockPos()))) {
            IVertexBuilder builder = buffer.getBuffer(RenderType.entityTranslucentCull(AtlasTexture.LOCATION_BLOCKS));

            Matrix4f posMat = matrixStack.last().pose();
            for (TankRenderInfo tankRenderInfo : getTanksToRender(te)) {
                doRender(builder, tankRenderInfo, posMat, combinedLightIn, combinedOverlayIn);
            }
        }
    }

    private void doRender(IVertexBuilder builder, TankRenderInfo tankRenderInfo, Matrix4f posMat, int combinedLight, int combinedOverlay) {
        IFluidTank tank = tankRenderInfo.getTank();
        if (tank.getFluidAmount() == 0) return;

        Fluid fluid = tank.getFluid().getFluid();
        ResourceLocation texture = fluid.getAttributes().getStillTexture(tank.getFluid());
        //noinspection deprecation
        TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(texture);
        int[] cols = RenderUtils.decomposeColor(fluid.getAttributes().getColor(tank.getFluid()));

        AxisAlignedBB bounds = getRenderBounds(tank, tankRenderInfo.getBounds());
        float x1 = (float) bounds.minX;
        float x2 = (float) bounds.maxX;
        float y1 = (float) bounds.minY;
        float y2 = (float) bounds.maxY;
        float z1 = (float) bounds.minZ;
        float z2 = (float) bounds.maxZ;
        double bx1 = bounds.minX * 16;
        double bx2 = bounds.maxX * 16;
        double by1 = bounds.minY * 16;
        double by2 = bounds.maxY * 16;
        double bz1 = bounds.minZ * 16;
        double bz2 = bounds.maxZ * 16;
        
        if (tankRenderInfo.shouldRender(Direction.DOWN)) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            builder.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, -1f, 0f).endVertex();
            builder.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, -1f, 0f).endVertex();
            builder.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, -1f, 0f).endVertex();
            builder.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, -1f, 0f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.UP)) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            builder.vertex(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
            builder.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.NORTH)) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(by1);
            float v2 = still.getV(by2);
            builder.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, -1f).endVertex();
            builder.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, -1f).endVertex();
            builder.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, -1f).endVertex();
            builder.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, -1f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.SOUTH)) {
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(by1);
            float v2 = still.getV(by2);
            builder.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
            builder.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.WEST)) {
            float u1 = still.getU(by1);
            float u2 = still.getU(by2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            builder.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(-1f, 0f, 0f).endVertex();
            builder.vertex(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(-1f, 0f, 0f).endVertex();
            builder.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(-1f, 0f, 0f).endVertex();
            builder.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(-1f, 0f, 0f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.EAST)) {
            float u1 = still.getU(by1);
            float u2 = still.getU(by2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            builder.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1f, 0f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1f, 0f, 0f).endVertex();
            builder.vertex(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u2, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1f, 0f, 0f).endVertex();
            builder.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).uv(u1, v2).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1f, 0f, 0f).endVertex();
        }
    }

    private AxisAlignedBB getRenderBounds(IFluidTank tank, AxisAlignedBB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY, y2 = (tankBounds.minY + (tankHeight * percent));
        if (tank.getFluid().getFluid().getAttributes().isLighterThanAir()) {
            double yOff = tankBounds.maxY - y2;  // lighter than air fluids move to the top of the tank
            y1 += yOff; y2 += yOff;
        }
        return new AxisAlignedBB(tankBounds.minX, y1, tankBounds.minZ, tankBounds.maxX, y2, tankBounds.maxZ);
    }

    static AxisAlignedBB rotateY(AxisAlignedBB in, int rot) {
        // clockwise rotation about the Y axis
        switch (rot) {
            case 90: return new AxisAlignedBB(1 - in.minZ, in.minY, in.minX, 1 - in.maxZ, in.maxY, in.maxX);
            case 180: return new AxisAlignedBB(1 - in.minX, in.minY, 1 - in.minZ, 1 - in.maxX, in.maxY, 1 - in.maxZ);
            case 270: return new AxisAlignedBB(in.minZ, in.minY, 1 - in.minX, in.maxZ, in.maxY, 1 - in.maxX);
            default: throw new IllegalArgumentException("rot must be 90, 180 or 270");
        }
    }

    abstract Collection<TankRenderInfo> getTanksToRender(T te);
}