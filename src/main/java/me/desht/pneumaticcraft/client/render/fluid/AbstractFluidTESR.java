package me.desht.pneumaticcraft.client.render.fluid;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;

import java.util.List;

public abstract class AbstractFluidTESR<T extends TileEntityBase> extends TileEntityRenderer<T> {
    AbstractFluidTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(T te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.getWorld().isAreaLoaded(te.getPos(), 0)) {
            IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());

            Matrix4f posMat = matrixStack.getLast().getMatrix();
            for (TankRenderInfo tankRenderInfo : getTanksToRender(te)) {
                doRender(te, builder, tankRenderInfo, posMat);
            }
        }
    }

    private void doRender(T te, IVertexBuilder builder, TankRenderInfo tankRenderInfo, Matrix4f posMat) {
        IFluidTank tank = tankRenderInfo.getTank();
        if (tank.getFluidAmount() == 0) return;

        World w = Minecraft.getInstance().world;
        Fluid fluid = tank.getFluid().getFluid();
        ResourceLocation texture = fluid.getAttributes().getStillTexture(tank.getFluid());
        //noinspection deprecation
        TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
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
            int downCombined = WorldRenderer.getCombinedLight(w, te.getPos().down());
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            builder.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v2).lightmap(downCombined).normal(0f, -1f, 0f).endVertex();
            builder.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v1).lightmap(downCombined).normal(0f, -1f, 0f).endVertex();
            builder.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v1).lightmap(downCombined).normal(0f, -1f, 0f).endVertex();
            builder.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v2).lightmap(downCombined).normal(0f, -1f, 0f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.UP)) {
            int upCombined = WorldRenderer.getCombinedLight(w, te.getPos().up());
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            builder.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v2).lightmap(upCombined).normal(0f, 1f, 0f).endVertex();
            builder.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v2).lightmap(upCombined).normal(0f, 1f, 0f).endVertex();
            builder.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v1).lightmap(upCombined).normal(0f, 1f, 0f).endVertex();
            builder.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v1).lightmap(upCombined).normal(0f, 1f, 0f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.NORTH)) {
            int northCombined = WorldRenderer.getCombinedLight(w, te.getPos().north());
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(by1);
            float v2 = still.getInterpolatedV(by2);
            builder.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v1).lightmap(northCombined).normal(0f, 0f, -1f).endVertex();
            builder.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v2).lightmap(northCombined).normal(0f, 0f, -1f).endVertex();
            builder.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v2).lightmap(northCombined).normal(0f, 0f, -1f).endVertex();
            builder.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v1).lightmap(northCombined).normal(0f, 0f, -1f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.SOUTH)) {
            int southCombined = WorldRenderer.getCombinedLight(w, te.getPos().south());
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(by1);
            float v2 = still.getInterpolatedV(by2);
            builder.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v1).lightmap(southCombined).normal(0f, 0f, 1f).endVertex();
            builder.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v2).lightmap(southCombined).normal(0f, 0f, 1f).endVertex();
            builder.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v2).lightmap(southCombined).normal(0f, 0f, 1f).endVertex();
            builder.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v1).lightmap(southCombined).normal(0f, 0f, 1f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.WEST)) {
            int westCombined = WorldRenderer.getCombinedLight(w, te.getPos().west());
            float u1 = still.getInterpolatedU(by1);
            float u2 = still.getInterpolatedU(by2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            builder.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v2).lightmap(westCombined).normal(-1f, 0f, 0f).endVertex();
            builder.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v2).lightmap(westCombined).normal(-1f, 0f, 0f).endVertex();
            builder.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v1).lightmap(westCombined).normal(-1f, 0f, 0f).endVertex();
            builder.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v1).lightmap(westCombined).normal(-1f, 0f, 0f).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.EAST)) {
            int eastCombined = WorldRenderer.getCombinedLight(w, te.getPos().east());
            float u1 = still.getInterpolatedU(by1);
            float u2 = still.getInterpolatedU(by2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            builder.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v1).lightmap(eastCombined).normal(1f, 0f, 0f).endVertex();
            builder.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v1).lightmap(eastCombined).normal(1f, 0f, 0f).endVertex();
            builder.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u2, v2).lightmap(eastCombined).normal(1f, 0f, 0f).endVertex();
            builder.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).tex(u1, v2).lightmap(eastCombined).normal(1f, 0f, 0f).endVertex();
        }
    }

    private AxisAlignedBB getRenderBounds(IFluidTank tank, AxisAlignedBB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY, y2 = (tankBounds.minY + (tankHeight * percent));
        if (tank.getFluid().getFluid().getAttributes().getDensity() < 0) {
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

    abstract List<TankRenderInfo> getTanksToRender(T te);
}