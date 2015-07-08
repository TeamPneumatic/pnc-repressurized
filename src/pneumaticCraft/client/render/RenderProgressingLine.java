package pneumaticCraft.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiSecurityStationBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RenderProgressingLine{
    public double startX;
    public double startY;
    public double startZ;
    public double endX;
    public double endY;
    public double endZ;
    protected float progress = 0;

    public RenderProgressingLine(){}

    public RenderProgressingLine(double startX, double startY, double startZ, double endX, double endY, double endZ){
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public RenderProgressingLine(double startX, double startY, double endX, double endY){
        this(startX, startY, 0, endX, endY, 0);
    }

    public RenderProgressingLine(RenderProgressingLine copy){
        this(copy.startX, copy.startY, copy.startZ, copy.endX, copy.endY, copy.endZ);
        progress = copy.progress;
    }

    public boolean hasLineSameProperties(double startX, double startY, double startZ, double endX, double endY, double endZ){
        return Math.abs(startX - this.startX) < 0.01D && Math.abs(startY - this.startY) < 0.01D && Math.abs(startZ - this.startZ) < 0.01D && Math.abs(endX - this.endX) < 0.01D && Math.abs(endY - this.endY) < 0.01D && Math.abs(endZ - this.endZ) < 0.01D;
    }

    public float getProgress(){
        return progress;
    }

    public RenderProgressingLine setProgress(float progress){
        this.progress = progress;
        return this;
    }

    /**
     * Increases the progress by the given amount.
     * @param increment
     * @return Returns true when the maximum progress has been reached.
     */
    public boolean incProgress(float increment){
        progress += increment;
        if(progress > 1F) {
            progress = 1F;
            return true;
        } else if(progress < 0F) {
            progress = 0F;
            return true;
        }
        return false;
    }

    public boolean incProgressByDistance(double distance){
        double totalDistance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) + Math.pow(endZ - startZ, 2));
        progress += distance / totalDistance;
        if(progress > 1F) {
            progress = 1F;
            return true;
        } else if(progress < 0F) {
            progress = 0F;
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void render(){
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(startX, startY, startZ);
        tess.addVertex(startX + (endX - startX) * progress, startY + (endY - startY) * progress, startZ + (endZ - startZ) * progress);
        tess.draw();
    }

    @SideOnly(Side.CLIENT)
    public void renderInterpolated(RenderProgressingLine lastTickLine, float partialTick){
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(getInter(startX, lastTickLine.startX, partialTick), getInter(startY, lastTickLine.startY, partialTick), getInter(startZ, lastTickLine.startZ, partialTick));
        tess.addVertex(getInter(startX, lastTickLine.startX, partialTick) + (getInter(endX, lastTickLine.endX, partialTick) - getInter(startX, lastTickLine.startX, partialTick)) * progress, getInter(startY, lastTickLine.startY, partialTick) + (getInter(startY, lastTickLine.startY, partialTick) - getInter(endY, lastTickLine.endY, partialTick)) * progress, getInter(startZ, lastTickLine.startZ, partialTick) + (getInter(endZ, lastTickLine.endZ, partialTick) - getInter(startZ, lastTickLine.startZ, partialTick)) * progress);
        tess.draw();
    }

    protected double getInter(double cur, double old, float partialTick){
        return old + (cur - old) * partialTick;
    }

    public int getPointedSlotNumber(GuiSecurityStationBase gui){
        Slot slot = gui.getSlotAtPosition((int)endX, (int)endY);
        return slot != null ? slot.slotNumber : 0;
    }
}
