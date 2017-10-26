package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;

public class RenderRefinery extends TileEntitySpecialRenderer<TileEntityRefinery> {
    @Override
    public void render(TileEntityRefinery te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getOilTank().getFluidAmount() == 0 && te.getOutputTank().getFluidAmount() == 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureMap map =  Minecraft.getMinecraft().getTextureMapBlocks();
        Tessellator tess = Tessellator.getInstance();

        FluidTank oilTank = te.getOilTank();
        if (oilTank.getFluidAmount() > 0) {
            float percent = (float) oilTank.getFluidAmount() / (float) oilTank.getCapacity();
            TextureAtlasSprite sprite = map.getAtlasSprite(oilTank.getFluid().getFluid().getStill().toString());
            AxisAlignedBB bounds = getBoundsForRender(te.getRotation(), percent);
            if (bounds != null) doRender(tess, sprite, bounds);
        }

        FluidTank outputTank = te.getOutputTank();
        if (outputTank.getFluidAmount() > 0) {
            float percent = (float) outputTank.getFluidAmount() / (float) outputTank.getCapacity();
            TextureAtlasSprite sprite = map.getAtlasSprite(outputTank.getFluid().getFluid().getStill().toString());
            AxisAlignedBB bounds = getBoundsForRender(te.getRotation().getOpposite(), percent);
            if (bounds != null) doRender(tess, sprite, bounds);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private AxisAlignedBB getBoundsForRender(EnumFacing rotation, float percent) {
        switch (rotation) {
            case NORTH:
                return new AxisAlignedBB(4.25f/16f, 1f/16f, 13f/16f, 11.75f/16f, (1f + 14f * percent) / 16f, 15.75f/16f);
            case SOUTH:
                return new AxisAlignedBB(4.25f/16f, 1f/16f, 0.25f/16f, 11.75f/16f, (1f + 14f * percent)/16f, 3f/16f);
            case EAST:
                return new AxisAlignedBB(0.25f/16f, 1f/16f, 4.25f/16f, 3f/16f, (1f + 14f * percent)/16f, 11.75f/16f);
            case WEST:
                return new AxisAlignedBB(13f/16f, 1f/16f, 4.25f/16f, 15.75f/16f, (1f + 14f * percent)/16f, 11.75f/16f);
            default:
                // shouldn't happen but TE crescent wrench is able to rotate it onto its side...
                return null;
//                throw new IllegalArgumentException("only horizontal rotations expected!");
        }
    }

    private void doRender(Tessellator tess, TextureAtlasSprite sprite, AxisAlignedBB bounds) {

        BufferBuilder buffer = tess.getBuffer();

        // south face
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(bounds.minX, bounds.minY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.maxY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.minY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        tess.draw();

        // north face
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.minY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        tess.draw();

        // west face
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(bounds.minX, bounds.minY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxZ), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxZ), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.maxY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minZ), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.minY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minZ), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        tess.draw();

        // east face
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(bounds.maxX, bounds.minY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minZ), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minZ), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxZ), sprite.getInterpolatedV(16 * bounds.maxY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxZ), sprite.getInterpolatedV(16 * bounds.minY))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        tess.draw();

        // top face
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(bounds.minX, bounds.maxY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.maxZ))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.maxY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.maxZ))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.maxY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.minZ))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.maxY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.minZ))
                .color(1.0f, 1.0f, 1.0f, 0.7f)
                .endVertex();
        tess.draw();

        // bottom face
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(bounds.minX, bounds.minY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.maxZ))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.minY, bounds.maxZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.maxZ))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.maxX, bounds.minY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.maxX), sprite.getInterpolatedV(16 * bounds.minZ))
                .color(1.0f, 1.0f, 1.0f, 0.75f)
                .endVertex();
        buffer.pos(bounds.minX, bounds.minY, bounds.minZ)
                .tex(sprite.getInterpolatedU(16 * bounds.minX), sprite.getInterpolatedV(16 * bounds.minZ))
                .color(1.0f, 1.0f, 1.0f, 0.7f)
                .endVertex();
        tess.draw();
    }
}
