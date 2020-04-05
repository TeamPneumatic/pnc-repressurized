package me.desht.pneumaticcraft.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable pre, Runnable post) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, pre, post);
    }

//    private static final VertexFormat POSITION_COLOR_NORMAL = new VertexFormat(
//            ImmutableList.<VertexFormatElement>builder()
//                    .add(DefaultVertexFormats.POSITION_3F)
//                    .add(DefaultVertexFormats.COLOR_4UB)
//                    .add(DefaultVertexFormats.NORMAL_3B)
//                    .build()
//    );

    public static final RenderType TEXTURE = makeType("texture",
            DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .lightmap(LIGHTMAP_DISABLED)
                    .build(false)
    );

    public static final RenderType BLOCK_TRACKER = makeType("block_tracker",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(LineState.DEFAULT_LINE)
                    .layer(RenderState.PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_ALWAYS)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false)
    );

    // drone ai (quads, pos/tex, blend)

    // animated_stat

    // target_circle

    // block_outline

    // logistics_frame

    // drone_frame

    // minigun tracers (lines, pos/color, no texture, pre/post stipple)

    // drone laser (quads, color/tex, blend, no lighting, no cull)

    // progress bar (quads, pos/color, no texture, shade smooth)

    // progress bar & animated stat outline (line loop, pos/color, no textture, shade smooth)

    // elevator caller (quads, pos/color, no texture, no lighting, blend)
}
