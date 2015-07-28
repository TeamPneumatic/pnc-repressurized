package pneumaticCraft.client;

import java.util.Set;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.ChunkPosition;

import org.lwjgl.opengl.GL11;

public class AreaShowHandler{
    private final Set<ChunkPosition> showingPositions;
    private final int color;
    private int renderList;

    public AreaShowHandler(Set<ChunkPosition> area, int color){
        showingPositions = area;
        this.color = color;
        compileRenderList();
    }

    private void compileRenderList(){
        renderList = GL11.glGenLists(1);
        GL11.glNewList(renderList, GL11.GL_COMPILE);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_I(color, 150);

        for(ChunkPosition pos : showingPositions) {
            t.addTranslation(pos.chunkPosX + 0.25F, pos.chunkPosY + 0.25F, pos.chunkPosZ + 0.25F);

            t.addVertex(0, 0, 0);
            t.addVertex(0, 0.5, 0);
            t.addVertex(0.5, 0.5, 0);
            t.addVertex(0.5, 0, 0);

            t.addVertex(0.5, 0, 0.5);
            t.addVertex(0.5, 0.5, 0.5);
            t.addVertex(0, 0.5, 0.5);
            t.addVertex(0, 0, 0.5);

            t.addVertex(0, 0, 0);
            t.addVertex(0, 0, 0.5);
            t.addVertex(0, 0.5, 0.5);
            t.addVertex(0, 0.5, 0);

            t.addVertex(0.5, 0.5, 0);
            t.addVertex(0.5, 0.5, 0.5);
            t.addVertex(0.5, 0, 0.5);
            t.addVertex(0.5, 0, 0);

            t.addVertex(0, 0, 0);
            t.addVertex(0.5, 0, 0);
            t.addVertex(0.5, 0, 0.5);
            t.addVertex(0, 0, 0.5);

            t.addVertex(0, 0.5, 0.5);
            t.addVertex(0.5, 0.5, 0.5);
            t.addVertex(0.5, 0.5, 0);
            t.addVertex(0, 0.5, 0);

            t.addTranslation(-pos.chunkPosX - 0.25F, -pos.chunkPosY - 0.25F, -pos.chunkPosZ - 0.25F);
        }

        t.draw();

        t.startDrawing(GL11.GL_LINES);
        t.setColorRGBA_I(0, 150);

        for(ChunkPosition pos : showingPositions) {
            t.addTranslation(pos.chunkPosX + 0.25F, pos.chunkPosY + 0.25F, pos.chunkPosZ + 0.25F);

            t.addVertex(0, 0, 0);
            t.addVertex(0, 0.5, 0);
            t.addVertex(0.5, 0.5, 0);
            t.addVertex(0.5, 0, 0);

            t.addVertex(0.5, 0, 0.5);
            t.addVertex(0.5, 0.5, 0.5);
            t.addVertex(0, 0.5, 0.5);
            t.addVertex(0, 0, 0.5);

            t.addVertex(0, 0, 0);
            t.addVertex(0, 0, 0.5);
            t.addVertex(0, 0.5, 0.5);
            t.addVertex(0, 0.5, 0);

            t.addVertex(0.5, 0.5, 0);
            t.addVertex(0.5, 0.5, 0.5);
            t.addVertex(0.5, 0, 0.5);
            t.addVertex(0.5, 0, 0);

            t.addVertex(0, 0, 0);
            t.addVertex(0.5, 0, 0);
            t.addVertex(0.5, 0, 0.5);
            t.addVertex(0, 0, 0.5);

            t.addVertex(0, 0.5, 0.5);
            t.addVertex(0.5, 0.5, 0.5);
            t.addVertex(0.5, 0.5, 0);
            t.addVertex(0, 0.5, 0);

            t.addTranslation(-pos.chunkPosX - 0.25F, -pos.chunkPosY - 0.25F, -pos.chunkPosZ - 0.25F);
        }

        t.draw();
        GL11.glEndList();
    }

    public void render(){
        GL11.glCallList(renderList);
    }
}
