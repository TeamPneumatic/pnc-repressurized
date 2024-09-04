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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
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
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setTextureState(textureState)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .createCompositeState(false);
        return create("texture", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                256, false, false, state);
    });
    public static RenderType getTextureRender(ResourceLocation texture) {
        return TEXTURE_RENDER.apply(texture);
    }

    private static final BiFunction<ResourceLocation, Boolean, RenderType> TEXTURE_RENDER_COLORED = Util.memoize((rl, disableDepthTest) -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(rl, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        return create("texture_color", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                256, false, false, state);
    });
    public static RenderType getTextureRenderColored(ResourceLocation texture) {
        return getTextureRenderColored(texture, false);
    }
    public static RenderType getTextureRenderColored(ResourceLocation texture, boolean disableDepthTest) {
        return TEXTURE_RENDER_COLORED.apply(texture, disableDepthTest);
    }

    public static final RenderType UNTEXTURED_QUAD_NO_DEPTH = create("untextured_quad_no_depth", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
            false, false,
            RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType UNTEXTURED_QUAD = create("untextured_quad", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
            false, false,
            RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_FRAME = create("block_frame",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
            false, false,
            RenderType.CompositeState.builder()
                    .setTextureState(NO_TEXTURE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_TRACKER = create("block_tracker",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256,
            false, false,
            RenderType.CompositeState.builder()
                    .setLineState(LineStateShard.DEFAULT_LINE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
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
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final Function<Double,RenderType> LINE_LOOPS = Util.memoize((lineWidth) -> {
        LineStateShard lineState = new LineStateShard(OptionalDouble.of(lineWidth));
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setLineState(lineState)
                .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(NO_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        return create("line_loops",
                DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.DEBUG_LINE_STRIP, 256,
                false, false,
                state);
    });
    public static RenderType getLineLoops(double lineWidth) {
        return LINE_LOOPS.apply(lineWidth);
    }

    private final static BiFunction<Boolean,Boolean,RenderType> BLOCK_HILIGHT_FACE = Util.memoize((disableDepthTest, disableWriteMask) -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setLightmapState(NO_LIGHTMAP)
                .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .setWriteMaskState(disableWriteMask ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                .createCompositeState(false);
        return create("nav_path_lines",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
                false, false, state);
    });
    public static RenderType getBlockHilightFace(boolean disableDepthTest, boolean disableWriteMask) {
        return BLOCK_HILIGHT_FACE.apply(disableDepthTest, disableWriteMask);
    }

    private final static BiFunction<Boolean,Boolean,RenderType> BLOCK_HILIGHT_LINE = Util.memoize((disableDepthTest, disableWriteMask) -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : RenderStateShard.LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setWriteMaskState(disableWriteMask ? COLOR_WRITE : RenderStateShard.COLOR_DEPTH_WRITE)
                .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                .createCompositeState(false);
        return create("block_hilight_line",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 256,
                false, false, state);
    });
    public static RenderType getBlockHilightLine(boolean disableDepthTest, boolean disableWriteMask) {
        return BLOCK_HILIGHT_LINE.apply(disableDepthTest, disableWriteMask);
    }

    public static final RenderType TRIANGLE_FAN = create("triangle_fan",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN, 256,
            false, false,
            RenderType.CompositeState.builder()
                    .setLineState(new LineStateShard(OptionalDouble.of(2.0)))
                    .setTextureState(NO_TEXTURE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false)
    );
}
