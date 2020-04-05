package me.desht.pneumaticcraft.client.render.fluid;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
//        if (!te.getWorld().getChunkProvider().getChunk(te.getPos().getX() >> 4, te.getPos().getZ() >> 4, true).isEmpty()) {
            IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());

            for (TankRenderInfo tankRenderInfo : getTanksToRender(te)) {
                doRender(te, builder, tankRenderInfo);
            }
        }
    }

    private void doRender(T te, IVertexBuilder buffer, TankRenderInfo tankRenderInfo) {
        IFluidTank tank = tankRenderInfo.getTank();
        if (tank.getFluidAmount() == 0) return;

        World w = Minecraft.getInstance().world;
        Fluid fluid = tank.getFluid().getFluid();
        ResourceLocation texture = fluid.getAttributes().getStillTexture(tank.getFluid());
        //noinspection deprecation
        TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
        int color = fluid.getAttributes().getColor(tank.getFluid());
        float alpha = (color >> 24 & 0xFF) / 255F;
        float red = (color >> 16 & 0xFF) / 255F;
        float green = (color >> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;

//        buffer.setTranslation(x,y,z);

        AxisAlignedBB bounds = getRenderBounds(tank, tankRenderInfo.getBounds());
        double bx1 = bounds.minX * 16;
        double bx2 = bounds.maxX * 16;
        double by1 = bounds.minY * 16;
        double by2 = bounds.maxY * 16;
        double bz1 = bounds.minZ * 16;
        double bz2 = bounds.maxZ * 16;

        if (tankRenderInfo.shouldRender(Direction.DOWN)) {
            int downCombined = WorldRenderer.getCombinedLight(w, te.getPos().down());
            int downLMa = downCombined >> 16 & 65535;
            int downLMb = downCombined & 65535;
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(downLMa, downLMb).endVertex();
            buffer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(downLMa, downLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(downLMa, downLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(downLMa, downLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.UP)) {
            int upCombined = WorldRenderer.getCombinedLight(w, te.getPos().up());
            int upLMa = upCombined >> 16 & 65535;
            int upLMb = upCombined & 65535;
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(upLMa, upLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(upLMa, upLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(upLMa, upLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(upLMa, upLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.NORTH)) {
            int northCombined = WorldRenderer.getCombinedLight(w, te.getPos().north());
            int northLMa = northCombined >> 16 & 65535;
            int northLMb = northCombined & 65535;
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(by1);
            float v2 = still.getInterpolatedV(by2);
            buffer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(northLMa, northLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(northLMa, northLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(northLMa, northLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(northLMa, northLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.SOUTH)) {
            int southCombined = WorldRenderer.getCombinedLight(w, te.getPos().south());
            int southLMa = southCombined >> 16 & 65535;
            int southLMb = southCombined & 65535;
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(by1);
            float v2 = still.getInterpolatedV(by2);
            buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(southLMa, southLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(southLMa, southLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(southLMa, southLMb).endVertex();
            buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(southLMa, southLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.WEST)) {
            int westCombined = WorldRenderer.getCombinedLight(w, te.getPos().west());
            int westLMa = westCombined >> 16 & 65535;
            int westLMb = westCombined & 65535;
            float u1 = still.getInterpolatedU(by1);
            float u2 = still.getInterpolatedU(by2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            buffer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(westLMa, westLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(westLMa, westLMb).endVertex();
            buffer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(westLMa, westLMb).endVertex();
            buffer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(westLMa, westLMb).endVertex();
        }

        if (tankRenderInfo.shouldRender(Direction.EAST)) {
            int eastCombined = WorldRenderer.getCombinedLight(w, te.getPos().east());
            int eastLMa = eastCombined >> 16 & 65535;
            int eastLMb = eastCombined & 65535;
            float u1 = still.getInterpolatedU(by1);
            float u2 = still.getInterpolatedU(by2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            buffer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).tex(u1, v1).lightmap(eastLMa, eastLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).tex(u2, v1).lightmap(eastLMa, eastLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).tex(u2, v2).lightmap(eastLMa, eastLMb).endVertex();
            buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).tex(u1, v2).lightmap(eastLMa, eastLMb).endVertex();
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