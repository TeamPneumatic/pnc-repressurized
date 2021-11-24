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

package me.desht.pneumaticcraft.client.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class ModRenderTypes extends RenderType {

    public ModRenderTypes(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable pre, Runnable post) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, pre, post);
    }

    public static RenderType getTextureRender(ResourceLocation texture) {
        RenderState.TextureState textureState = new TextureState(texture, false, false);
        return create("texture", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.builder()
                        .setTextureState(textureState)
                        .setLightmapState(RenderState.LIGHTMAP)
                        .createCompositeState(false)
        );
    }

    public static RenderType getTextureRenderColored(ResourceLocation texture) {
        return getTextureRenderColored(texture, false);
    }

    public static RenderType getTextureRenderColored(ResourceLocation texture, boolean disableDepthTest) {
        RenderState.TextureState textureState = new TextureState(texture, false, false);
        return create("texture_color", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.builder()
                        .setTextureState(textureState)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(RenderState.LIGHTMAP)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .createCompositeState(false)
        );
    }

    public static RenderType getUntexturedQuad(boolean disableDepthTest) {
        return create("untextured_quad_" + disableDepthTest, DefaultVertexFormats.POSITION_COLOR_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.builder()
                        .setTextureState(RenderState.NO_TEXTURE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(RenderState.LIGHTMAP)
                        .setShadeModelState(RenderState.SMOOTH_SHADE)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .createCompositeState(false)
        );
    }

    private static final VertexFormat POS_COLOR_NORMAL_LIGHTMAP = new VertexFormat(ImmutableList.<VertexFormatElement>builder()
            .add(DefaultVertexFormats.ELEMENT_POSITION)
            .add(DefaultVertexFormats.ELEMENT_COLOR)
            .add(DefaultVertexFormats.ELEMENT_NORMAL)
            .add(DefaultVertexFormats.ELEMENT_UV2)
            .add(DefaultVertexFormats.ELEMENT_PADDING)
            .build()
    );
    public static RenderType getBlockFrame(boolean disableDepthTest) {
        return create("block_frame",
                POS_COLOR_NORMAL_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.builder()
                        .setTextureState(NO_TEXTURE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setShadeModelState(SMOOTH_SHADE)
                        .setDiffuseLightingState(DIFFUSE_LIGHTING)
                        .setLightmapState(LIGHTMAP)
                        .setCullState(NO_CULL)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .createCompositeState(false)
        );
    }

    public static final RenderType BLOCK_TRACKER = create("block_tracker",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.builder().setLineState(LineState.DEFAULT_LINE)
                    // TODO 1.16 can we use VIEW_OFFSET_Z_LAYERING ?
//                    .layer(RenderState.PROJECTION_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType TARGET_CIRCLE = create("target_circle",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_STRIP, 65536,
            RenderType.State.builder()
//                    .layer(PROJECTION_LAYERING)
                    .setShadeModelState(SMOOTH_SHADE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static RenderType getLineLoops(double lineWidth) {
        LineState lineState = new LineState(OptionalDouble.of(lineWidth));
        return create("line_loops",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_LOOP, 256,
                RenderType.State.builder()
                        .setLineState(lineState)
                        .setShadeModelState(RenderState.SMOOTH_SHADE)
                        .setTextureState(RenderState.NO_TEXTURE)
                        .setLightmapState(RenderState.NO_LIGHTMAP)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .createCompositeState(false)
        );
    }

    public static RenderType getLineLoopsTransparent(double lineWidth) {
        LineState lineState = new LineState(OptionalDouble.of(lineWidth));
        return create("line_loops",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_LOOP, 256,
                RenderType.State.builder()
                        .setLineState(lineState)
                        .setShadeModelState(RenderState.SMOOTH_SHADE)
                        .setTextureState(RenderState.NO_TEXTURE)
                        .setLightmapState(RenderState.NO_LIGHTMAP)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
    }

    private static final LineState LINE_5 = new LineState(OptionalDouble.of(5.0));
    public static RenderType getNavPath(boolean xRay, boolean quads) {
        DepthTestState d = xRay ? DepthTestState.NO_DEPTH_TEST : DepthTestState.LEQUAL_DEPTH_TEST;
        WriteMaskState w = xRay ? WriteMaskState.COLOR_WRITE : WriteMaskState.COLOR_DEPTH_WRITE;

        if (quads) {
            return create("nav_path_quads",
                    DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
                    RenderType.State.builder()
                            .setDepthTestState(d)
                            .setWriteMaskState(w)
                            .setTextureState(RenderState.NO_TEXTURE)
                            .setCullState(RenderState.NO_CULL)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .createCompositeState(false)
            );
        } else {
            return create("nav_path_lines",
                    DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_STRIP, 256,
                    RenderType.State.builder().setLineState(LINE_5)
                            .setDepthTestState(d)
                            .setWriteMaskState(w)
                            .setTextureState(RenderState.NO_TEXTURE)
                            .setCullState(RenderState.NO_CULL)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .createCompositeState(false)
            );
        }
    }

    public static RenderType getBlockHilightFace(boolean disableDepthTest, boolean disableWriteMask) {
        return create("block_hilight",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
                RenderType.State.builder()
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setLightmapState(NO_LIGHTMAP)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(disableWriteMask ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .createCompositeState(false));
    }

    private static final LineState LINE_3 = new LineState(OptionalDouble.of(3.0));
    public static RenderType getBlockHilightLine(boolean disableDepthTest, boolean disableWriteMask) {
        return create("block_hilight_line",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
                RenderType.State.builder().setLineState(LINE_3)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : RenderState.LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(disableWriteMask ? COLOR_WRITE : RenderState.COLOR_DEPTH_WRITE)
                        .createCompositeState(false));
    }


    private static final LineState LINE_2 = new LineState(OptionalDouble.of(2.0));
    public static final RenderType TRIANGLE_FAN = create("triangle_fan",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_FAN, 256,
            RenderType.State.builder()
                    .setLineState(LINE_2)
                    .setTextureState(NO_TEXTURE)
                    .createCompositeState(false)
    );

    public static RenderType getArmorTranslucentNoCull(ResourceLocation rl) {
        RenderType.State state = RenderType.State.builder().setTextureState(new RenderState.TextureState(rl, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
        return create("armor_translucent_no_cull", DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true, false, state);
    }
}
