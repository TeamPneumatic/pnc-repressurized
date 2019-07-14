package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidTank;

import java.util.BitSet;
import java.util.List;

public abstract class FastFluidTESR<T extends TileEntityBase> extends TileEntityRendererFast<T> {
    private static final float FLUID_ALPHA = 0.9f;

    @Override
    public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder buffer) {
        if (!te.getWorld().getChunkProvider().getChunk(te.getPos().getX() >> 4, te.getPos().getZ() >> 4, true).isEmpty()) {
            for (TankRenderInfo tankRenderInfo : getTanksToRender(te)) {
                doRender(te, x, y, z, buffer, tankRenderInfo);
            }
        }
    }

    private void doRender(T te, double x, double y, double z, BufferBuilder buffer, TankRenderInfo tankRenderInfo) {
        IFluidTank tank = tankRenderInfo.tank;
        if (tank.getFluidAmount() == 0) return;

        Fluid f = tank.getFluid().getFluid();
        TextureAtlasSprite still = Minecraft.getInstance().getTextureMap().getAtlasSprite(f.getStill().toString());
        float u1 = still.getMinU(), v1 = still.getMinV(), u2 = still.getMaxU(), v2 = still.getMaxV();

        buffer.setTranslation(x,y,z);

        AxisAlignedBB bounds = getRenderBounds(tank, tankRenderInfo.bounds);

        if (tankRenderInfo.shouldRender(Direction.DOWN)) {
            int downCombined = getWorld().getCombinedLight(te.getPos().down(), 0);
            int downLMa = downCombined >> 16 & 65535;
            int downLMb = downCombined & 65535;
            buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v2).lightmap(downLMa, downLMb).endVertex();
            buffer.pos(bounds.minX, bounds.minY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v1).lightmap(downLMa, downLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v1).lightmap(downLMa, downLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v2).lightmap(downLMa, downLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.UP)) {
            int upCombined = getWorld().getCombinedLight(te.getPos().up(), 0);
            int upLMa = upCombined >> 16 & 65535;
            int upLMb = upCombined & 65535;
            buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v2).lightmap(upLMa, upLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v2).lightmap(upLMa, upLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v1).lightmap(upLMa, upLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v1).lightmap(upLMa, upLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.NORTH)) {
            int northCombined = getWorld().getCombinedLight(te.getPos().north(), 0);
            int northLMa = northCombined >> 16 & 65535;
            int northLMb = northCombined & 65535;
            buffer.pos(bounds.minX, bounds.minY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v1).lightmap(northLMa, northLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v2).lightmap(northLMa, northLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v2).lightmap(northLMa, northLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v1).lightmap(northLMa, northLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.SOUTH)) {
            int southCombined = getWorld().getCombinedLight(te.getPos().south(), 0);
            int southLMa = southCombined >> 16 & 65535;
            int southLMb = southCombined & 65535;
            buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v1).lightmap(southLMa, southLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v2).lightmap(southLMa, southLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v2).lightmap(southLMa, southLMb).endVertex();
            buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v1).lightmap(southLMa, southLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.WEST)) {
            int westCombined = getWorld().getCombinedLight(te.getPos().west(), 0);
            int westLMa = westCombined >> 16 & 65535;
            int westLMb = westCombined & 65535;
            buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v2).lightmap(westLMa, westLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v2).lightmap(westLMa, westLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v1).lightmap(westLMa, westLMb).endVertex();
            buffer.pos(bounds.minX, bounds.minY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v1).lightmap(westLMa, westLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.EAST)) {
            int eastCombined = getWorld().getCombinedLight(te.getPos().east(), 0);
            int eastLMa = eastCombined >> 16 & 65535;
            int eastLMb = eastCombined & 65535;
            buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v1).lightmap(eastLMa, eastLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v1).lightmap(eastLMa, eastLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u2, v2).lightmap(eastLMa, eastLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(1.0f, 1.0f, 1.0f, FLUID_ALPHA).tex(u1, v2).lightmap(eastLMa, eastLMb).endVertex();
        }
    }

    private AxisAlignedBB getRenderBounds(IFluidTank tank, AxisAlignedBB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY, y2 = (tankBounds.minY + (tankHeight * percent));
        if (tank.getFluid().getFluid().getDensity() < 0) {
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

    class TankRenderInfo {
        final IFluidTank tank;
        final AxisAlignedBB bounds;
        final BitSet faces = new BitSet(6);

        TankRenderInfo(IFluidTank tank, AxisAlignedBB bounds, Direction... renderFaces) {
            this.tank = tank;
            this.bounds = bounds;
            if (renderFaces.length == 0) {
                faces.set(0, 6, true);
            } else {
                for (Direction face : renderFaces) {
                    faces.set(face.getIndex(), true);
                }
            }
        }

        TankRenderInfo without(Direction face) {
            faces.clear(face.getIndex());
            return this;
        }

        boolean shouldRender(Direction face) {
            return faces.get(face.getIndex());
        }
    }
}