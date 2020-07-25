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
        return makeType("texture", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.getBuilder()
                        .texture(textureState)
                        .lightmap(RenderState.LIGHTMAP_ENABLED)
                        .build(false)
        );
    }

    public static RenderType getTextureRenderColored(ResourceLocation texture) {
        return getTextureRenderColored(texture, false);
    }

    public static RenderType getTextureRenderColored(ResourceLocation texture, boolean disableDepthTest) {
        RenderState.TextureState textureState = new TextureState(texture, false, false);
        return makeType("texture_color", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.getBuilder()
                        .texture(textureState)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .lightmap(RenderState.LIGHTMAP_ENABLED)
                        .depthTest(disableDepthTest ? DEPTH_ALWAYS : DEPTH_LEQUAL)
                        .writeMask(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .build(false)
        );
    }

    public static RenderType getUntexturedQuad(boolean disableDepthTest) {
        return makeType("untextured_quad_" + disableDepthTest, DefaultVertexFormats.POSITION_COLOR_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.getBuilder()
                        .texture(RenderState.NO_TEXTURE)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .cull(CULL_DISABLED)
                        .lightmap(RenderState.LIGHTMAP_ENABLED)
                        .shadeModel(RenderState.SHADE_ENABLED)
                        .depthTest(disableDepthTest ? DEPTH_ALWAYS : DEPTH_LEQUAL)
                        .writeMask(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .build(false)
        );
    }

    private static final VertexFormat POS_COLOR_NORMAL_LIGHTMAP = new VertexFormat(ImmutableList.<VertexFormatElement>builder()
            .add(DefaultVertexFormats.POSITION_3F)
            .add(DefaultVertexFormats.COLOR_4UB)
            .add(DefaultVertexFormats.NORMAL_3B)
            .add(DefaultVertexFormats.TEX_2SB)
            .add(DefaultVertexFormats.PADDING_1B)
            .build()
    );
    public static RenderType getBlockFrame(boolean disableDepthTest) {
        return makeType("block_frame",
                POS_COLOR_NORMAL_LIGHTMAP, GL11.GL_QUADS, 256,
                RenderType.State.getBuilder()
                        .texture(NO_TEXTURE)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .shadeModel(SHADE_ENABLED)
                        .diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
                        .lightmap(LIGHTMAP_ENABLED)
                        .cull(CULL_ENABLED)
                        .depthTest(disableDepthTest ? DEPTH_ALWAYS : DEPTH_LEQUAL)
                        .writeMask(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .build(false)
        );
    }

    public static final RenderType BLOCK_TRACKER = makeType("block_tracker",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(LineState.DEFAULT_LINE)
                    // TODO 1.16 can we use field_239235_M_ ?
//                    .layer(RenderState.PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .depthTest(DEPTH_ALWAYS)
                    .writeMask(COLOR_WRITE)
                    .build(false)
    );

    public static final RenderType TARGET_CIRCLE = makeType("target_circle",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_STRIP, 65536,
            RenderType.State.getBuilder()
//                    .layer(PROJECTION_LAYERING)
                    .shadeModel(SHADE_ENABLED)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .depthTest(DEPTH_ALWAYS)
                    .writeMask(COLOR_WRITE)
                    .build(false)
    );

    public static RenderType getLineLoops(double lineWidth) {
        LineState lineState = new LineState(OptionalDouble.of(lineWidth));
        return makeType("line_loops",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_LOOP, 256,
                RenderType.State.getBuilder()
                        .line(lineState)
                        .shadeModel(RenderState.SHADE_ENABLED)
                        .texture(RenderState.NO_TEXTURE)
                        .lightmap(RenderState.LIGHTMAP_DISABLED)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .build(false)
        );
    }

    public static RenderType getLineLoopsTransparent(double lineWidth) {
        LineState lineState = new LineState(OptionalDouble.of(lineWidth));
        return makeType("line_loops",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_LOOP, 256,
                RenderType.State.getBuilder()
                        .line(lineState)
                        .shadeModel(RenderState.SHADE_ENABLED)
                        .texture(RenderState.NO_TEXTURE)
                        .lightmap(RenderState.LIGHTMAP_DISABLED)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .depthTest(DEPTH_ALWAYS)
                        .writeMask(COLOR_WRITE)
                        .cull(CULL_DISABLED)
                        .build(false)
        );
    }

    private static final LineState LINE_5 = new LineState(OptionalDouble.of(5.0));
    public static RenderType getNavPath(boolean xRay, boolean quads) {
        DepthTestState d = xRay ? DepthTestState.DEPTH_ALWAYS : DepthTestState.DEPTH_LEQUAL;
        WriteMaskState w = xRay ? WriteMaskState.COLOR_WRITE : WriteMaskState.COLOR_DEPTH_WRITE;

        if (quads) {
            return makeType("nav_path_quads",
                    DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
                    RenderType.State.getBuilder()
                            .depthTest(d)
                            .writeMask(w)
                            .texture(RenderState.NO_TEXTURE)
                            .cull(RenderState.CULL_DISABLED)
                            .transparency(TRANSLUCENT_TRANSPARENCY)
                            .build(false)
            );
        } else {
            return makeType("nav_path_lines",
                    DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_STRIP, 256,
                    RenderType.State.getBuilder().line(LINE_5)
                            .depthTest(d)
                            .writeMask(w)
                            .texture(RenderState.NO_TEXTURE)
                            .cull(RenderState.CULL_DISABLED)
                            .transparency(TRANSLUCENT_TRANSPARENCY)
                            .build(false)
            );
        }
    }

    public static RenderType getBlockHilightFace(boolean disableDepthTest) {
        return makeType("block_hilight",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
                RenderType.State.getBuilder()
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .texture(NO_TEXTURE)
                        .lightmap(LIGHTMAP_DISABLED)
                        .depthTest(disableDepthTest ? DEPTH_ALWAYS : DEPTH_LEQUAL)
                        .writeMask(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                        .build(false));
    }

    private static final LineState LINE_3 = new LineState(OptionalDouble.of(3.0));
    public static RenderType getBlockHilightLine(boolean disableDepthTest) {
        return makeType("block_hilight_line",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
                RenderType.State.getBuilder().line(LINE_3)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .texture(NO_TEXTURE)
                        .depthTest(disableDepthTest ? DEPTH_ALWAYS : RenderState.DEPTH_LEQUAL)
                        .cull(CULL_DISABLED)
                        .lightmap(LIGHTMAP_DISABLED)
                        .writeMask(disableDepthTest ? COLOR_WRITE : RenderState.COLOR_DEPTH_WRITE)
                        .build(false));
    }


    private static final LineState LINE_2 = new LineState(OptionalDouble.of(2.0));
    public static final RenderType TRIANGLE_FAN = makeType("triangle_fan",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_FAN, 256,
            RenderType.State.getBuilder()
                    .line(LINE_2)
                    .texture(NO_TEXTURE)
                    .build(false)
    );
}
