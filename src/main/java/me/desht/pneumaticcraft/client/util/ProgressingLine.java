package me.desht.pneumaticcraft.client.util;

import me.desht.pneumaticcraft.client.gui.GuiSecurityStationBase;
import net.minecraft.inventory.container.Slot;

public class ProgressingLine {
    public double startX;
    public double startY;
    public double startZ;
    public double endX;
    public double endY;
    public double endZ;
    protected float progress = 0;

    public ProgressingLine() {
    }

    public ProgressingLine(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public ProgressingLine(double startX, double startY, double endX, double endY) {
        this(startX, startY, 0, endX, endY, 0);
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

//    @OnlyIn(Dist.CLIENT)
//    public void render(MatrixStack matrixStack, IVertexBuilder builder, int color) {
////        BufferBuilder wr = Tessellator.getInstance().getBuffer();
////        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
//        int a = (color >> 24) & 0xFF;
//        int r = (color >> 16) & 0xFF;
//        int g = (color >> 8) & 0xFF;
//        int b = color & 0xFF;
//        builder.pos(startX, startY, startZ).color(r, g, b, a).endVertex();
//        builder.pos(startX + (endX - startX) * progress, startY + (endY - startY) * progress, startZ + (endZ - startZ) * progress).endVertex();
////        Tessellator.getInstance().draw();
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public void renderInterpolated(ProgressingLine prev, float partialTick, MatrixStack matrixStack, IVertexBuilder builder, int color) {
////        BufferBuilder wr = Tessellator.getInstance().getBuffer();
////        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
//        int a = (color >> 24) & 0xFF;
//        int r = (color >> 16) & 0xFF;
//        int g = (color >> 8) & 0xFF;
//        int b = color & 0xFF;
//        builder.pos(lerp(partialTick, startX, prev.startX),
//                lerp(partialTick, startY, prev.startY),
//                lerp(partialTick, startZ, prev.startZ))
//                .color(r, g, b, a)
//                .endVertex();
//        builder.pos(
//                lerp(partialTick, startX, prev.startX) + (lerp(partialTick, endX, prev.endX) - lerp(partialTick, startX, prev.startX)) * progress,
//                lerp(partialTick, startY, prev.startY) + (lerp(partialTick, startY, prev.startY) - lerp(partialTick, endY, prev.endY)) * progress,
//                lerp(partialTick, startZ, prev.startZ) + (lerp(partialTick, endZ, prev.endZ) - lerp(partialTick, startZ, prev.startZ)) * progress)
//                .color(r, g, b, a)
//                .endVertex();
////        Tessellator.getInstance().draw();
//    }

    public int getPointedSlotNumber(GuiSecurityStationBase gui) {
        Slot slot = gui.getSlotAtPosition((int) endX, (int) endY);
        return slot != null ? slot.slotNumber : 0;
    }
}
