package pneumaticCraft.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.ChunkPosition;

import org.lwjgl.opengl.GL11;

public class AreaShowHandler{
    private final Set<ChunkPosition> showingPositions;
    private final int color;
    private int ticksExisted;

    public AreaShowHandler(Set<ChunkPosition> area, int color){
        showingPositions = area;
        this.color = color;
    }

    public boolean update(){
        return ticksExisted++ < 200;
    }

    public void render(){

        Tessellator t = Tessellator.instance;
        for(ChunkPosition pos : showingPositions) {
            GL11.glPushMatrix();
            GL11.glTranslated(pos.chunkPosX + 0.25, pos.chunkPosY + 0.25, pos.chunkPosZ + 0.25);
            GL11.glScaled(0.5, 0.5, 0.5);
            t.startDrawingQuads();
            t.setColorRGBA_I(color, ticksExisted < 150 ? 150 : (200 - ticksExisted) * 3);

            t.addVertex(0, 0, 0);
            t.addVertex(0, 1, 0);
            t.addVertex(1, 1, 0);
            t.addVertex(1, 0, 0);

            t.addVertex(1, 0, 1);
            t.addVertex(1, 1, 1);
            t.addVertex(0, 1, 1);
            t.addVertex(0, 0, 1);

            t.addVertex(0, 0, 0);
            t.addVertex(0, 0, 1);
            t.addVertex(0, 1, 1);
            t.addVertex(0, 1, 0);

            t.addVertex(1, 1, 0);
            t.addVertex(1, 1, 1);
            t.addVertex(1, 0, 1);
            t.addVertex(1, 0, 0);

            t.addVertex(0, 0, 0);
            t.addVertex(1, 0, 0);
            t.addVertex(1, 0, 1);
            t.addVertex(0, 0, 1);

            t.addVertex(0, 1, 1);
            t.addVertex(1, 1, 1);
            t.addVertex(1, 1, 0);
            t.addVertex(0, 1, 0);

            t.draw();
            GL11.glPopMatrix();
        }
        if(Minecraft.getMinecraft().gameSettings.fancyGraphics) {
            for(ChunkPosition pos : showingPositions) {
                GL11.glPushMatrix();
                GL11.glTranslated(pos.chunkPosX + 0.25, pos.chunkPosY + 0.25, pos.chunkPosZ + 0.25);
                GL11.glScaled(0.5, 0.5, 0.5);
                t.startDrawing(GL11.GL_LINES);
                t.setColorRGBA_I(0, ticksExisted < 150 ? 150 : (200 - ticksExisted) * 3);

                t.addVertex(0, 0, 0);
                t.addVertex(0, 1, 0);
                t.addVertex(1, 1, 0);
                t.addVertex(1, 0, 0);

                t.addVertex(1, 0, 1);
                t.addVertex(1, 1, 1);
                t.addVertex(0, 1, 1);
                t.addVertex(0, 0, 1);

                t.addVertex(0, 0, 0);
                t.addVertex(0, 0, 1);
                t.addVertex(0, 1, 1);
                t.addVertex(0, 1, 0);

                t.addVertex(1, 1, 0);
                t.addVertex(1, 1, 1);
                t.addVertex(1, 0, 1);
                t.addVertex(1, 0, 0);

                t.addVertex(0, 0, 0);
                t.addVertex(1, 0, 0);
                t.addVertex(1, 0, 1);
                t.addVertex(0, 0, 1);

                t.addVertex(0, 1, 1);
                t.addVertex(1, 1, 1);
                t.addVertex(1, 1, 0);
                t.addVertex(0, 1, 0);

                t.draw();
                GL11.glPopMatrix();
            }
        }

    }
}
