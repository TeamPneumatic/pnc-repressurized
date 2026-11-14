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

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.NeoForgeRenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModRenderTypes {
    private static final DepthStencilState DEPTH_TEST_NO_WRITE = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false);

    private static final RenderPipeline.Snippet TEXTURED_LIGHTMAP_SNIPPET = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET)
            .withVertexShader("core/rendertype_text")
            .withFragmentShader("core/rendertype_text")
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .buildSnippet();

    private static final RenderPipeline.Snippet UNTEXTURED_LIGHTMAP_SNIPPET = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET)
            .withVertexShader("core/rendertype_text_background")
            .withFragmentShader("core/rendertype_text_background")
            .withSampler("Sampler2")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS)
            .withCull(false)
            .buildSnippet();

    private static final RenderPipeline.Snippet POSITION_COLOR_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .buildSnippet();

    public static final RenderPipeline TEXTURE_PIPELINE = RenderPipeline.builder(TEXTURED_LIGHTMAP_SNIPPET)
            .withLocation(RL("pipeline/texture"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();
    public static final RenderPipeline TEXTURE_COLORED_PIPELINE = RenderPipeline.builder(TEXTURED_LIGHTMAP_SNIPPET)
            .withLocation(RL("pipeline/texture_color"))
            .build();
    public static final RenderPipeline TEXTURE_COLORED_NO_DEPTH_PIPELINE = RenderPipeline.builder(TEXTURED_LIGHTMAP_SNIPPET)
            .withLocation(RL("pipeline/texture_color_no_depth"))
            .withDepthStencilState(Optional.empty())
            .build();
    public static final RenderPipeline UNTEXTURED_QUAD_PIPELINE = RenderPipeline.builder(UNTEXTURED_LIGHTMAP_SNIPPET)
            .withLocation(RL("pipeline/untextured_quad"))
            .build();
    public static final RenderPipeline UNTEXTURED_QUAD_NO_DEPTH_PIPELINE = RenderPipeline.builder(UNTEXTURED_LIGHTMAP_SNIPPET)
            .withLocation(RL("pipeline/untextured_quad_no_depth"))
            .withDepthStencilState(Optional.empty())
            .build();
    public static final RenderPipeline TARGET_CIRCLE_PIPELINE = RenderPipeline.builder(UNTEXTURED_LIGHTMAP_SNIPPET)
            .withLocation(RL("pipeline/target_circle"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLE_STRIP)
            .withDepthStencilState(Optional.empty())
            .build();
    public static final RenderPipeline BLOCK_FRAME_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation(RL("pipeline/block_frame"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(Optional.empty())
            .build();
    public static final RenderPipeline HILIGHT_FACE_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation(RL("pipeline/hilight_face"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();
    public static final RenderPipeline HILIGHT_FACE_NO_DEPTH_WRITE_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation(RL("pipeline/hilight_face_no_depth_write"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DEPTH_TEST_NO_WRITE)
            .build();
    public static final RenderPipeline HILIGHT_FACE_NO_DEPTH_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation(RL("pipeline/hilight_face_no_depth"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(Optional.empty())
            .build();
    public static final RenderPipeline TRIANGLE_FAN_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation(RL("pipeline/triangle_fan"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
            .build();
    public static final RenderPipeline TRIANGLES_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation(RL("pipeline/triangles"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build();
    public static final RenderPipeline LINES_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(RL("pipeline/lines"))
            .build();
    public static final RenderPipeline LINES_NO_DEPTH_WRITE_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(RL("pipeline/lines_no_depth_write"))
            .withDepthStencilState(DEPTH_TEST_NO_WRITE)
            .build();
    public static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(RL("pipeline/lines_no_depth"))
            .withDepthStencilState(Optional.empty())
            .build();

    private static final List<RenderPipeline> PIPELINES = List.of(
            TEXTURE_PIPELINE, TEXTURE_COLORED_PIPELINE, TEXTURE_COLORED_NO_DEPTH_PIPELINE,
            UNTEXTURED_QUAD_PIPELINE, UNTEXTURED_QUAD_NO_DEPTH_PIPELINE, TARGET_CIRCLE_PIPELINE,
            BLOCK_FRAME_PIPELINE, HILIGHT_FACE_PIPELINE, HILIGHT_FACE_NO_DEPTH_WRITE_PIPELINE, HILIGHT_FACE_NO_DEPTH_PIPELINE,
            TRIANGLE_FAN_PIPELINE, TRIANGLES_PIPELINE,
            LINES_PIPELINE, LINES_NO_DEPTH_WRITE_PIPELINE, LINES_NO_DEPTH_PIPELINE
    );

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        PIPELINES.forEach(event::registerPipeline);
    }

    private static RenderType simple(String name, RenderPipeline pipeline) {
        return RenderType.create(name, RenderSetup.builder(pipeline).createRenderSetup());
    }

    private static RenderType lightmapped(String name, RenderPipeline pipeline) {
        return RenderType.create(name, RenderSetup.builder(pipeline).useLightmap().createRenderSetup());
    }

    private static final Function<Identifier, RenderType> TEXTURE_RENDER = Util.memoize(texture ->
            RenderType.create("texture", RenderSetup.builder(TEXTURE_PIPELINE)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                    .createRenderSetup()));
    public static RenderType getTextureRender(Identifier texture) {
        return TEXTURE_RENDER.apply(texture);
    }

    private static final BiFunction<Identifier, Boolean, RenderType> TEXTURE_RENDER_COLORED = Util.memoize((texture, disableDepthTest) ->
            RenderType.create("texture_color", RenderSetup.builder(disableDepthTest ? TEXTURE_COLORED_NO_DEPTH_PIPELINE : TEXTURE_COLORED_PIPELINE)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .createRenderSetup()));
    public static RenderType getTextureRenderColored(Identifier texture) {
        return getTextureRenderColored(texture, false);
    }
    public static RenderType getTextureRenderColored(Identifier texture, boolean disableDepthTest) {
        return TEXTURE_RENDER_COLORED.apply(texture, disableDepthTest);
    }

    public static final RenderType UNTEXTURED_QUAD_NO_DEPTH = lightmapped("untextured_quad_no_depth", UNTEXTURED_QUAD_NO_DEPTH_PIPELINE);

    public static final RenderType UNTEXTURED_QUAD = lightmapped("untextured_quad", UNTEXTURED_QUAD_PIPELINE);

    public static final RenderType BLOCK_FRAME = simple("block_frame", BLOCK_FRAME_PIPELINE);

    public static final RenderType BLOCK_TRACKER = simple("block_tracker", LINES_NO_DEPTH_PIPELINE);

    public static final RenderType TARGET_CIRCLE = RenderType.create("target_circle", RenderSetup.builder(TARGET_CIRCLE_PIPELINE)
            .useLightmap()
            .bufferSize(65536)
            .createRenderSetup());

    /**
     * Lines drawn without depth testing. Vertices are submitted in pairs (one pair per line segment), and since
     * line width is now a vertex attribute, each vertex also needs a normal and a line width.
     */
    public static final RenderType LINES_NO_DEPTH = simple("pnc_lines_no_depth", LINES_NO_DEPTH_PIPELINE);

    private final static BiFunction<Boolean, Boolean, RenderType> BLOCK_HILIGHT_FACE = Util.memoize((disableDepthTest, disableWriteMask) ->
            simple("block_hilight_face", disableDepthTest ? HILIGHT_FACE_NO_DEPTH_PIPELINE :
                    disableWriteMask ? HILIGHT_FACE_NO_DEPTH_WRITE_PIPELINE : HILIGHT_FACE_PIPELINE));
    public static RenderType getBlockHilightFace(boolean disableDepthTest, boolean disableWriteMask) {
        return BLOCK_HILIGHT_FACE.apply(disableDepthTest, disableWriteMask);
    }

    private final static BiFunction<Boolean, Boolean, RenderType> BLOCK_HILIGHT_LINE = Util.memoize((disableDepthTest, disableWriteMask) ->
            simple("block_hilight_line", disableDepthTest ? LINES_NO_DEPTH_PIPELINE :
                    disableWriteMask ? LINES_NO_DEPTH_WRITE_PIPELINE : LINES_PIPELINE));
    public static RenderType getBlockHilightLine(boolean disableDepthTest, boolean disableWriteMask) {
        return BLOCK_HILIGHT_LINE.apply(disableDepthTest, disableWriteMask);
    }

    private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(texture ->
            RenderType.create("pnc_entity_translucent_cull", RenderSetup.builder(NeoForgeRenderPipelines.ENTITY_TRANSLUCENT_CULL)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup()));
    /**
     * Same as the 1.21 vanilla entityTranslucentCull render type, which 26.1 no longer has.
     */
    public static RenderType getEntityTranslucentCull(Identifier texture) {
        return ENTITY_TRANSLUCENT_CULL.apply(texture);
    }

    public static final RenderType TRIANGLE_FAN = simple("triangle_fan", TRIANGLE_FAN_PIPELINE);

    public static final RenderType TRIANGLES = simple("triangles", TRIANGLES_PIPELINE);
}
