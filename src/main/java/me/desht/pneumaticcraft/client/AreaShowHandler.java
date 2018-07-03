package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Set;

public class AreaShowHandler {
    private final Set<BlockPos> showingPositions;
    private final int color;
    private final double size;
    private final int renderList;
    private final boolean disableDepthTest;

    AreaShowHandler(Set<BlockPos> area, int color, double size, boolean disableDepthTest) {
        this.showingPositions = area;
        this.color = color;
        this.size = size;
        this.disableDepthTest = disableDepthTest;
        this.renderList = compileRenderList();
    }

    AreaShowHandler(Set<BlockPos> area, int color, boolean disableDepthTest) {
        this(area, color, 0.5, disableDepthTest);
    }

    private int compileRenderList() {
        int renderList = GlStateManager.glGenLists(1);
        GlStateManager.glNewList(renderList, GL11.GL_COMPILE);

        if(disableDepthTest) GlStateManager.disableDepth();
        
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        RenderUtils.glColorHex(color);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        double start = (1 - size) / 2.0;
        
        for (BlockPos pos : showingPositions) {
            wr.setTranslation(pos.getX() + start, pos.getY() + start, pos.getZ() + start);

            wr.pos(0, 0, 0).endVertex();
            wr.pos(0, size, 0).endVertex();
            wr.pos(size, size, 0).endVertex();
            wr.pos(size, 0, 0).endVertex();

            wr.pos(size, 0, size).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(0, size, size).endVertex();
            wr.pos(0, 0, size).endVertex();

            wr.pos(0, 0, 0).endVertex();
            wr.pos(0, 0, size).endVertex();
            wr.pos(0, size, size).endVertex();
            wr.pos(0, size, 0).endVertex();

            wr.pos(size, size, 0).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(size, 0, size).endVertex();
            wr.pos(size, 0, 0).endVertex();

            wr.pos(0, 0, 0).endVertex();
            wr.pos(size, 0, 0).endVertex();
            wr.pos(size, 0, size).endVertex();
            wr.pos(0, 0, size).endVertex();

            wr.pos(0, size, size).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(size, size, 0).endVertex();
            wr.pos(0, size, 0).endVertex();
        }

        Tessellator.getInstance().draw();

        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        RenderUtils.glColorHex(0X404040, 128);

        for (BlockPos pos : showingPositions) {
            wr.setTranslation(pos.getX() + start, pos.getY() + start, pos.getZ() + start);

            wr.pos(0, 0, 0).endVertex();
            wr.pos(0, size, 0).endVertex();
            wr.pos(size, size, 0).endVertex();
            wr.pos(size, 0, 0).endVertex();

            wr.pos(size, 0, size).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(0, size, size).endVertex();
            wr.pos(0, 0, size).endVertex();

            wr.pos(0, 0, 0).endVertex();
            wr.pos(0, 0, size).endVertex();
            wr.pos(0, size, size).endVertex();
            wr.pos(0, size, 0).endVertex();

            wr.pos(size, size, 0).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(size, 0, size).endVertex();
            wr.pos(size, 0, 0).endVertex();

            wr.pos(0, 0, 0).endVertex();
            wr.pos(size, 0, 0).endVertex();
            wr.pos(size, 0, size).endVertex();
            wr.pos(0, 0, size).endVertex();

            wr.pos(0, size, size).endVertex();
            wr.pos(size, size, size).endVertex();
            wr.pos(size, size, 0).endVertex();
            wr.pos(0, size, 0).endVertex();
        }

        wr.setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();
        
        if(disableDepthTest) GlStateManager.enableDepth();
        
        GlStateManager.glEndList();
        return renderList;
    }

    public void render() {
        /*if(disableDepthTest){
            GlStateManager.disableDepth();
            GlStateManager.callList(renderList);
            GlStateManager.enableDepth();
        }else{*/
            GlStateManager.callList(renderList);
        //}
    }
}
