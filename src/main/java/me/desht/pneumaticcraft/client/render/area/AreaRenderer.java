package me.desht.pneumaticcraft.client.render.area;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Set;

public class AreaRenderer {
    private final Set<BlockPos> showingPositions;
    private final int color;
    private final float size;
    private final boolean disableDepthTest;
    private final boolean drawShapes;
    private final boolean drawFaces;
    private final boolean disableWriteMask;

    private AreaRenderer(Set<BlockPos> area, int color, float size, boolean disableDepthTest, boolean drawShapes, boolean disableWriteMask, boolean drawFaces) {
        this.showingPositions = area;
        this.color = color;
        this.size = size;
        this.disableDepthTest = disableDepthTest;
        this.disableWriteMask = disableWriteMask;
        this.drawShapes = drawShapes;
        this.drawFaces = drawFaces;
    }

//    AreaRenderer(Set<BlockPos> area, int color, boolean disableDepthTest) {
//        this(area, color, 0.5f, disableDepthTest, false, disableDepthTest);
//    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        if (drawFaces) {
            RenderType type = ModRenderTypes.getBlockHilightFace(disableDepthTest, disableWriteMask);
            render(matrixStack, buffer.getBuffer(type));
            RenderUtils.finishBuffer(buffer, type);
        }

        RenderType type = ModRenderTypes.getBlockHilightLine(disableDepthTest, disableWriteMask);
        render(matrixStack, buffer.getBuffer(type));
        RenderUtils.finishBuffer(buffer, type);
    }

    private void render(MatrixStack matrixStack, IVertexBuilder builder) {
        int[] cols = RenderUtils.decomposeColor(color);
        for (BlockPos pos : showingPositions) {
            matrixStack.push();
            if (drawShapes) {
                matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
            } else {
                double start = (1 - size) / 2.0;
                matrixStack.translate(pos.getX() + start, pos.getY() + start, pos.getZ() + start);
            }
            Matrix4f posMat = matrixStack.getLast().getMatrix();
            addVertices(builder, posMat, pos, cols);
            matrixStack.pop();
        }
    }

    private void addVertices(IVertexBuilder wr, Matrix4f posMat, BlockPos pos, int[] cols) {
        World world = Minecraft.getInstance().world;
        BlockState state = world.getBlockState(pos);
        boolean xray = disableDepthTest || disableWriteMask;
        if (!xray && !state.getMaterial().isReplaceable()) return;
        if (drawShapes) {
            VoxelShape shape = state.getBlock() instanceof BlockPneumaticCraftCamo ?
                    ((BlockPneumaticCraftCamo) state.getBlock()).getUncamouflagedShape(state, world, pos, ISelectionContext.dummy()) :
                    state.getShape(world, pos, ISelectionContext.dummy());
            shape.forEachBox((x1d, y1d, z1d, x2d, y2d, z2d) -> {
                float x1 = (float) x1d;
                float x2 = (float) x2d;
                float y1 = (float) y1d;
                float y2 = (float) y2d;
                float z1 = (float) z1d;
                float z2 = (float) z2d;
                wr.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

                wr.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

                wr.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

                wr.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

                wr.pos(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

                wr.pos(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
                wr.pos(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            });
        } else {
            wr.pos(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

            wr.pos(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

            wr.pos(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

            wr.pos(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

            wr.pos(posMat, 0, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, 0, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, 0, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();

            wr.pos(posMat, 0, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, size, size).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, size, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            wr.pos(posMat, 0, size, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
        }
    }

    static Builder builder() {
        return new AreaRenderer.Builder();
    }

    static class Builder {
        private int color = 0x40808080;
        private float size = 0.5f;
        private boolean disableDepthTest = false;
        private boolean drawShapes = false;
        private boolean disableWriteMask = false;
        private boolean drawFaces = true;

        Builder withColor(int color) {
            this.color = color;
            return this;
        }

        Builder withSize(float size) {
            this.size = size;
            return this;
        }

        Builder disableDepthTest() {
            this.disableDepthTest = true;
            return this;
        }

        Builder disableWriteMask() {
            this.disableWriteMask = true;
            return this;
        }

        Builder xray() {
            this.disableWriteMask = disableDepthTest = true;
            return this;
        }

        Builder drawShapes() {
            this.drawShapes = true;
            return this;
        }

        Builder outlineOnly() {
            this.drawFaces = false;
            return this;
        }

        AreaRenderer build(Set<BlockPos> area) {
            return new AreaRenderer(area, color, size, disableDepthTest, drawShapes, disableWriteMask, drawFaces);
        }

        AreaRenderer build(BlockPos pos) {
            return build(Collections.singleton(pos));
        }
    }
}
