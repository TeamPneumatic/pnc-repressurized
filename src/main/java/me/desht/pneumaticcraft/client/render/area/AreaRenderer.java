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

package me.desht.pneumaticcraft.client.render.area;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

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

    public void render(PoseStack matrixStack, MultiBufferSource buffer) {
        if (drawFaces) {
            RenderType type = ModRenderTypes.getBlockHilightFace(disableDepthTest, disableWriteMask);
            VertexConsumer builder = buffer.getBuffer(type);
            forEachShape(matrixStack, shape -> addFaceVertices(builder, matrixStack.last().pose(), shape));
            RenderUtils.finishBuffer(buffer, type);
        }

        RenderType type = ModRenderTypes.getBlockHilightLine(disableDepthTest, disableWriteMask);
        VertexConsumer builder = buffer.getBuffer(type);
        float lineWidth = RenderUtils.defaultLineWidth();
        forEachShape(matrixStack, shape -> ShapeRenderer.renderShape(matrixStack, builder, shape, 0, 0, 0, color, lineWidth));
        RenderUtils.finishBuffer(buffer, type);
    }

    private void forEachShape(PoseStack matrixStack, Consumer<VoxelShape> consumer) {
        Level level = ClientUtils.getClientLevel();
        boolean xray = disableDepthTest || disableWriteMask;
        VoxelShape cube = Shapes.box(0, 0, 0, size, size, size);
        for (BlockPos pos : showingPositions) {
            BlockState state = level.getBlockState(pos);
            if (!xray && !state.canBeReplaced()) continue;
            matrixStack.pushPose();
            VoxelShape shape;
            if (drawShapes) {
                matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
                shape = state.getBlock() instanceof AbstractCamouflageBlock c ?
                        c.getUncamouflagedShape(state, level, pos, CollisionContext.empty()) :
                        state.getShape(level, pos, CollisionContext.empty());
            } else {
                double start = (1 - size) / 2.0;
                matrixStack.translate(pos.getX() + start, pos.getY() + start, pos.getZ() + start);
                shape = cube;
            }
            consumer.accept(shape);
            matrixStack.popPose();
        }
    }

    private void addFaceVertices(VertexConsumer wr, Matrix4f posMat, VoxelShape shape) {
        int[] cols = RenderUtils.decomposeColor(color);
        shape.forAllBoxes((x1d, y1d, z1d, x2d, y2d, z2d) -> {
            float x1 = (float) x1d;
            float x2 = (float) x2d;
            float y1 = (float) y1d;
            float y2 = (float) y2d;
            float z1 = (float) z1d;
            float z2 = (float) z2d;
            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]);

            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]);

            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]);

            wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]);

            wr.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]);

            wr.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
            wr.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]);
        });
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
