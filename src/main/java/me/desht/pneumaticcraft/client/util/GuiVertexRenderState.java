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

package me.desht.pneumaticcraft.client.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A GUI element made of arbitrary vertices, for the custom 2D geometry (lines, graphs, fluid tiles...) that used to be
 * drawn straight to a Tesselator. Vertices are in GUI coordinates; the pose at submit time is applied when building.
 */
public record GuiVertexRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                   List<Vertex> vertices, @Nullable ScreenRectangle scissorArea,
                                   @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        boolean hasUv = pipeline.getVertexFormat().contains(VertexFormatElement.UV0);
        for (Vertex v : vertices) {
            vertexConsumer.addVertexWith2DPose(pose, v.x(), v.y());
            if (hasUv) vertexConsumer.setUv(v.u(), v.v());
            vertexConsumer.setColor(v.color());
        }
    }

    /**
     * Untextured quads (four vertices each) with the default GUI pipeline.
     */
    public static void submit(GuiGraphicsExtractor graphics, Consumer<Builder> consumer) {
        submit(graphics, RenderPipelines.GUI, TextureSetup.noTexture(), consumer);
    }

    public static void submit(GuiGraphicsExtractor graphics, RenderPipeline pipeline, TextureSetup textureSetup, Consumer<Builder> consumer) {
        Builder builder = new Builder();
        consumer.accept(builder);
        if (builder.vertices.isEmpty()) return;

        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = graphics.peekScissorStack();
        ScreenRectangle bounds = new ScreenRectangle(
                (int) Math.floor(builder.minX), (int) Math.floor(builder.minY),
                (int) Math.ceil(builder.maxX - Math.floor(builder.minX)), (int) Math.ceil(builder.maxY - Math.floor(builder.minY))
        ).transformMaxBounds(pose);
        if (scissor != null) bounds = scissor.intersection(bounds);

        graphics.submitGuiElementRenderState(new GuiVertexRenderState(pipeline, textureSetup, pose, List.copyOf(builder.vertices), scissor, bounds));
    }

    /**
     * Convert a width in screen pixels (like the old {@code RenderSystem.lineWidth()}) into GUI units.
     */
    public static float pixels(float px) {
        return px / Minecraft.getInstance().getWindow().getGuiScale();
    }

    public static TextureSetup textureSetup(Identifier texture) {
        AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(texture);
        return TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler());
    }

    public record Vertex(float x, float y, float u, float v, int color) {
    }

    public static class Builder {
        private final List<Vertex> vertices = new ArrayList<>();
        private float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        public Builder vertex(float x, float y, int color) {
            return vertex(x, y, 0f, 0f, color);
        }

        public Builder vertex(float x, float y, float u, float v, int color) {
            vertices.add(new Vertex(x, y, u, v, color));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            return this;
        }

        /**
         * A line segment, as a quad of the given width (GUI pipelines are quad-based, and GL line width is gone).
         */
        public Builder line(float x1, float y1, float x2, float y2, float width, int color) {
            return line(x1, y1, x2, y2, width, color, color);
        }

        /**
         * A triangle, as a degenerate quad (the last vertex repeated).
         */
        public Builder triangle(float x1, float y1, int color1, float x2, float y2, int color2, float x3, float y3, int color3) {
            vertex(x1, y1, color1);
            vertex(x2, y2, color2);
            vertex(x3, y3, color3);
            return vertex(x3, y3, color3);
        }

        public Builder line(float x1, float y1, float x2, float y2, float width, int color1, int color2) {
            float dx = x2 - x1, dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len == 0f) return this;
            float nx = -dy / len * width / 2f, ny = dx / len * width / 2f;
            vertex(x1 - nx, y1 - ny, color1);
            vertex(x1 + nx, y1 + ny, color1);
            vertex(x2 + nx, y2 + ny, color2);
            vertex(x2 - nx, y2 - ny, color2);
            return this;
        }
    }
}
