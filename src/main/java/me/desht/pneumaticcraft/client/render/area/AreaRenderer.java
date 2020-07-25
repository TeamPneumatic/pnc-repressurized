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

import java.util.Set;

public class AreaRenderer {
    private final Set<BlockPos> showingPositions;
    private final int color;
    private final float size;
    private final boolean disableDepthTest;
    private final boolean drawShapes;

    AreaRenderer(Set<BlockPos> area, int color, float size, boolean disableDepthTest, boolean drawShapes) {
        this.showingPositions = area;
        this.color = color;
        this.size = size;
        this.disableDepthTest = disableDepthTest;
        this.drawShapes = drawShapes;
    }

    AreaRenderer(Set<BlockPos> area, int color, boolean disableDepthTest) {
        this(area, color, 0.5f, disableDepthTest, false);
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        RenderType type = ModRenderTypes.getBlockHilightFace(disableDepthTest);
        render(matrixStack, buffer.getBuffer(type));
        RenderUtils.finishBuffer(buffer, type);

        type = ModRenderTypes.getBlockHilightLine(disableDepthTest);
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
        if (!disableDepthTest && !state.getMaterial().isReplaceable()) return;
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
}
