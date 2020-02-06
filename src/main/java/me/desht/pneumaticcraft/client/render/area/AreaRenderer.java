package me.desht.pneumaticcraft.client.render.area;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

import java.util.Set;

public class AreaRenderer {
    private final Set<BlockPos> showingPositions;
    private final int color;
    private final double size;
    private final int renderList;
    private final boolean disableDepthTest;
    private final boolean drawShapes;

    AreaRenderer(Set<BlockPos> area, int color, double size, boolean disableDepthTest, boolean drawShapes) {
        this.showingPositions = area;
        this.color = color;
        this.size = size;
        this.disableDepthTest = disableDepthTest;
        this.drawShapes = drawShapes;
        this.renderList = compileRenderList();
    }

    AreaRenderer(Set<BlockPos> area, int color, boolean disableDepthTest) {
        this(area, color, 0.5, disableDepthTest, false);
    }

    private int compileRenderList() {
        int renderList = GlStateManager.genLists(1);
        GlStateManager.newList(renderList, GL11.GL_COMPILE);

        if (disableDepthTest) GlStateManager.disableDepthTest();
        
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        RenderUtils.glColorHex(color);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        for (BlockPos pos : showingPositions) {
            doRender(wr, pos);
        }
        Tessellator.getInstance().draw();

        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        RenderUtils.glColorHex(0X404040, 128);

        for (BlockPos pos : showingPositions) {
            doRender(wr, pos);
        }
        wr.setTranslation(0, 0, 0);
        Tessellator.getInstance().draw();
        
        if (disableDepthTest) GlStateManager.enableDepthTest();
        
        GlStateManager.endList();
        return renderList;
    }

    private void doRender(BufferBuilder wr, BlockPos pos) {
        if (drawShapes) {
            wr.setTranslation(pos.getX(), pos.getY(), pos.getZ());
            BlockState state = Minecraft.getInstance().world.getBlockState(pos);
            VoxelShape shape = state.getShape(Minecraft.getInstance().world, pos, SELECTION_CONTEXT);
            shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
                wr.pos(x1, y1, z1).endVertex();
                wr.pos(x1, y2, z1).endVertex();
                wr.pos(x2, y2, z1).endVertex();
                wr.pos(x2, y1, z1).endVertex();

                wr.pos(x2, y1, z2).endVertex();
                wr.pos(x2, y2, z2).endVertex();
                wr.pos(x1, y2, z2).endVertex();
                wr.pos(x1, y1, z2).endVertex();

                wr.pos(x1, y1, z1).endVertex();
                wr.pos(x1, y1, z2).endVertex();
                wr.pos(x1, y2, z2).endVertex();
                wr.pos(x1, y2, z1).endVertex();

                wr.pos(x2, y2, z1).endVertex();
                wr.pos(x2, y2, z2).endVertex();
                wr.pos(x2, y1, z2).endVertex();
                wr.pos(x2, y1, z1).endVertex();

                wr.pos(x1, y1, z1).endVertex();
                wr.pos(x2, y1, z1).endVertex();
                wr.pos(x2, y1, z2).endVertex();
                wr.pos(x1, y1, z2).endVertex();

                wr.pos(x1, y2, z2).endVertex();
                wr.pos(x2, y2, z2).endVertex();
                wr.pos(x2, y2, z1).endVertex();
                wr.pos(x1, y2, z1).endVertex();
            });
        } else {
            double start = (1 - size) / 2.0;
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
    }

    public void render() {
        GlStateManager.callList(renderList);
    }

    private static final ISelectionContext SELECTION_CONTEXT = new ISelectionContext() {
        @Override
        public boolean isSneaking() {
            return false;
        }

        @Override
        public boolean func_216378_a(VoxelShape shape, BlockPos pos, boolean p_216378_3_) {
            return false;
        }

        @Override
        public boolean hasItem(Item itemIn) {
            return false;
        }
    };

}
