package pneumaticCraft.client.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.util.RenderUtils;

public class RenderRangeLines{
    private final List<RenderProgressingLine> rangeLines = new ArrayList<RenderProgressingLine>();
    private int rangeLinesTimer = 0;
    private static Random rand = new Random();
    private final int color;

    public RenderRangeLines(int color){
        this.color = color;
    }

    public void resetRendering(double range){
        rangeLinesTimer = 200;

        rangeLines.clear();
        double renderRange = range + 0.5D;
        for(int i = 0; i < range * 16 + 8; i++) {
            //Add the vertical lines of the walls
            rangeLines.add(new RenderProgressingLine(-renderRange + i / 8D, -renderRange + 1, -renderRange, -renderRange + i / 8D, renderRange + 1, -renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, -renderRange + 1, renderRange, renderRange - i / 8D, renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + 1, renderRange - i / 8D, -renderRange, renderRange + 1, renderRange - i / 8D));
            rangeLines.add(new RenderProgressingLine(renderRange, -renderRange + 1, -renderRange + i / 8D, renderRange, renderRange + 1, -renderRange + i / 8D));

            //Add the horizontal lines of the walls
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + i / 8D + 1, -renderRange, -renderRange, -renderRange + i / 8D + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange, -renderRange + i / 8D + 1, -renderRange, renderRange, -renderRange + i / 8D + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, renderRange - i / 8D + 1, -renderRange, renderRange, renderRange - i / 8D + 1, -renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + i / 8D + 1, renderRange, renderRange, -renderRange + i / 8D + 1, renderRange));

            //Add the roof and floor
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, -renderRange + 1, -renderRange, renderRange - i / 8D, -renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, renderRange + 1, -renderRange, renderRange - i / 8D, renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + 1, -renderRange + i / 8D, renderRange, -renderRange + 1, -renderRange + i / 8D));
            rangeLines.add(new RenderProgressingLine(-renderRange, renderRange + 1, -renderRange + i / 8D, renderRange, renderRange + 1, -renderRange + i / 8D));

        }
    }

    public boolean isCurrentlyRendering(){
        return rangeLines.size() > 0;
    }

    public void update(){
        if(rangeLinesTimer > 0) {
            rangeLinesTimer--;
            for(RenderProgressingLine line : rangeLines) {
                if(line.getProgress() > 0.005F || rand.nextInt(60) == 0) {
                    line.incProgress(0.01F);
                }
            }
        } else {
            Iterator<RenderProgressingLine> iterator = rangeLines.iterator();
            while(iterator.hasNext()) {
                RenderProgressingLine line = iterator.next();
                if(line.getProgress() > 0.005F) {
                    line.incProgress(0.01F);
                }
                if(rand.nextInt(10) == 0) {
                    iterator.remove();
                }
            }
        }
    }

    public void render(){
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtils.glColorHex(color);
        GL11.glLineWidth(1.0F);
        for(RenderProgressingLine line : rangeLines) {
            line.render();
        }
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
