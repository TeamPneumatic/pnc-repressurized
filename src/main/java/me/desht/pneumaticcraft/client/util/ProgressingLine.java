package me.desht.pneumaticcraft.client.util;

import me.desht.pneumaticcraft.client.gui.GuiSecurityStationBase;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class ProgressingLine {

    public float startX;
    public float startY;
    public float startZ;
    public float endX;
    public float endY;
    public float endZ;
    protected float progress = 0;

    public ProgressingLine() {
    }

    public ProgressingLine(Vector3f start, Vector3f end) {
        this.startX = start.getX();
        this.startX = start.getY();
        this.startX = start.getZ();
        this.endX = end.getX();
        this.endY = end.getY();
        this.endZ = end.getZ();
    }

    public ProgressingLine(Vector3d start, Vector3d end) {
        this(new Vector3f(start), new Vector3f(end));
    }

    public ProgressingLine(float startX, float startY, float startZ, float endX, float endY, float endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public ProgressingLine(float startX, float startY, float endX, float endY) {
        this(new Vector3f(startX, startY, 0f), new Vector3f(endX, endY, 0f));
    }

    public ProgressingLine(ProgressingLine copy) {
        this(copy.startX, copy.startY, copy.startZ, copy.endX, copy.endY, copy.endZ);
        progress = copy.progress;
    }

    public boolean hasLineSameProperties(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        return Math.abs(startX - this.startX) < 0.01D && Math.abs(startY - this.startY) < 0.01D && Math.abs(startZ - this.startZ) < 0.01D && Math.abs(endX - this.endX) < 0.01D && Math.abs(endY - this.endY) < 0.01D && Math.abs(endZ - this.endZ) < 0.01D;
    }

    public float getProgress() {
        return progress;
    }

    public ProgressingLine setProgress(float progress) {
        this.progress = progress;
        return this;
    }

    /**
     * Increases the progress by the given amount.
     *
     * @param increment
     * @return Returns true when the maximum progress has been reached.
     */
    public boolean incProgress(float increment) {
        progress += increment;
        if (progress > 1F) {
            progress = 1F;
            return true;
        } else if (progress < 0F) {
            progress = 0F;
            return true;
        }
        return false;
    }

    public boolean incProgressByDistance(double distance) {
        double totalDistance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) + Math.pow(endZ - startZ, 2));
        progress += distance / totalDistance;
        if (progress > 1F) {
            progress = 1F;
            return true;
        } else if (progress < 0F) {
            progress = 0F;
            return true;
        }
        return false;
    }

    public int getPointedSlotNumber(GuiSecurityStationBase gui) {
        Slot slot = gui.getSlotAtPosition((int) endX, (int) endY);
        return slot != null ? slot.slotNumber : 0;
    }
}
