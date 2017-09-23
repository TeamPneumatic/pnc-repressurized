package igwmod;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class TessWrapper{
    public static void startDrawingTexturedQuads(){
        Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
    }

    public static void addVertexWithUV(double x, double y, double z, double u, double v){
        Tessellator.getInstance().getBuffer().pos(x, y, z).tex(u, v).endVertex();
    }

    public static void draw(){
        Tessellator.getInstance().draw();
    }
}
