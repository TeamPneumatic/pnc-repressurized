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

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    private static final Function<ResourceLocation,RenderType> TEXTURE_RENDER = Util.memoize((rl) -> {
        RenderStateShard.TextureStateShard textureState = new TextureStateShard(rl, false, false);
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setTextureState(textureState)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                .createCompositeState(false);
        return create("texture", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
                false, false, rendertype$compositestate);
    });

    public static RenderType getTextureRender(ResourceLocation texture) {
        return TEXTURE_RENDER.apply(texture);
    }

    private static final BiFunction<ResourceLocation, Boolean, RenderType> TEXTURE_RENDER_COLORED = Util.memoize((rl, disableDepthTest) -> {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(rl, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        return create("texture_color", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                256, false, false,
                rendertype$compositestate);
    });

    public static RenderType getTextureRenderColored(ResourceLocation texture) {
        return getTextureRenderColored(texture, false);
    }

    public static RenderType getTextureRenderColored(ResourceLocation texture, boolean disableDepthTest) {
        return TEXTURE_RENDER_COLORED.apply(texture, disableDepthTest);
    }

    public static RenderType getUntexturedQuad(boolean disableDepthTest) {
        return create("untextured_quad_" + disableDepthTest, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setTextureState(RenderStateShard.NO_TEXTURE)
                        .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .createCompositeState(false)
        );
    }

    private static final VertexFormat POS_COLOR_NORMAL_LIGHTMAP = new VertexFormat(ImmutableMap.<String,VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
            .put("Padding", DefaultVertexFormat.ELEMENT_PADDING)
            .build()
    );
    public static RenderType getBlockFrame(boolean disableDepthTest) {
        return create("block_frame",
                POS_COLOR_NORMAL_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setTextureState(NO_TEXTURE)
                        .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                        .setShadeModelState(SMOOTH_SHADE)
//                        .setDiffuseLightingState(DIFFUSE_LIGHTING)
                        .setLightmapState(LIGHTMAP)
                        .setCullState(NO_CULL)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .createCompositeState(false)
        );
    }

    public static final RenderType BLOCK_TRACKER = create("block_tracker",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256,
            false, false,
            RenderType.CompositeState.builder().setLineState(LineStateShard.DEFAULT_LINE)
                    // TODO 1.16 can we use VIEW_OFFSET_Z_LAYERING ?
//                    .layer(RenderState.PROJECTION_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType TARGET_CIRCLE = create("target_circle",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLE_STRIP, 65536,
            false, false,
            RenderType.CompositeState.builder()
//                    .layer(PROJECTION_LAYERING)
//                    .setShadeModelState(SMOOTH_SHADE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static RenderType getLineLoops(double lineWidth) {
        LineStateShard lineState = new LineStateShard(OptionalDouble.of(lineWidth));
        return create("line_loops",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setLineState(lineState)
//                        .setShadeModelState(RenderStateShard.SMOOTH_SHADE)
                        .setLineState(lineState)
                        .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                        .setLayeringState(LayeringStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setTextureState(RenderStateShard.NO_TEXTURE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(OutputStateShard.ITEM_ENTITY_TARGET)
                        .createCompositeState(false)
        );
    }

    public static RenderType getLineLoopsTransparent(double lineWidth) {
        LineStateShard lineState = new LineStateShard(OptionalDouble.of(lineWidth));
        return create("line_loops",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setLineState(lineState)
                        .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                        .setTextureState(RenderStateShard.NO_TEXTURE)
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
    }

    private static final LineStateShard LINE_5 = new LineStateShard(OptionalDouble.of(5.0));
    public static RenderType getNavPath(boolean xRay, boolean quads) {
        DepthTestStateShard d = xRay ? DepthTestStateShard.NO_DEPTH_TEST : DepthTestStateShard.LEQUAL_DEPTH_TEST;
        WriteMaskStateShard w = xRay ? WriteMaskStateShard.COLOR_WRITE : WriteMaskStateShard.COLOR_DEPTH_WRITE;

        if (quads) {
            return create("nav_path_quads",
                    DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
                    false, false,
                    RenderType.CompositeState.builder()
                            .setDepthTestState(d)
                            .setWriteMaskState(w)
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setCullState(RenderStateShard.NO_CULL)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                            .createCompositeState(false)
            );
        } else {
            return create("nav_path_lines",
                    DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP, 256,
                    false, false,
                    RenderType.CompositeState.builder().setLineState(LINE_5)
                            .setDepthTestState(d)
                            .setWriteMaskState(w)
                            .setTextureState(RenderStateShard.NO_TEXTURE)
                            .setCullState(RenderStateShard.NO_CULL)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                            .createCompositeState(false)
            );
        }
    }

    public static RenderType getBlockHilightFace(boolean disableDepthTest, boolean disableWriteMask) {
        return create("block_hilight",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
                false, false,
                RenderType.CompositeState.builder()
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setLightmapState(NO_LIGHTMAP)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(disableWriteMask ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                        .createCompositeState(false));
    }

    private static final LineStateShard LINE_3 = new LineStateShard(OptionalDouble.of(3.0));
    public static RenderType getBlockHilightLine(boolean disableDepthTest, boolean disableWriteMask) {
        return create("block_hilight_line",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 256,
                false, false,
                RenderType.CompositeState.builder().setLineState(LINE_3)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(disableWriteMask ? COLOR_WRITE : RenderStateShard.COLOR_DEPTH_WRITE)
                        .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                        .createCompositeState(false));
    }


    private static final LineStateShard LINE_2 = new LineStateShard(OptionalDouble.of(2.0));
    public static final RenderType TRIANGLE_FAN = create("triangle_fan",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN, 256,
            false, false,
            RenderType.CompositeState.builder()
                    .setLineState(LINE_2)
                    .setTextureState(NO_TEXTURE)
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .createCompositeState(false)
    );

    private static final Function<ResourceLocation, RenderType> ARMOR_TRANSLUCENT_NO_CULL = Util.memoize((rl) -> {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(rl, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true);
        return create("armor_translucent_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, true, false,
                rendertype$compositestate);
    });
    public static RenderType getArmorTranslucentNoCull(ResourceLocation rl) {
        return ARMOR_TRANSLUCENT_NO_CULL.apply(rl);
    }

//    public static RenderType getArmorTranslucentNoCull(ResourceLocation rl) {
//        RenderType.CompositeState state = RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(rl, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
//        return create("armor_translucent_no_cull", DefaultVertexFormat.NEW_ENTITY, GL11.GL_QUADS, 256, true, false, state);
//    }
}
