package me.desht.pneumaticcraft.client.render.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.IFluidTank;

class TankRenderHelper {
    static AxisAlignedBB getRenderBounds(IFluidTank tank, AxisAlignedBB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY, y2 = (tankBounds.minY + (tankHeight * percent));
        if (tank.getFluid().getFluid().getDensity() < 0) {
            double yOff = tankBounds.maxY - y2;  // lighter than air fluids move to the top of the tank
            y1 += yOff; y2 += yOff;
        }
        return new AxisAlignedBB(tankBounds.minX, y1, tankBounds.minZ, tankBounds.maxX, y2, tankBounds.maxZ);
    }

    static void doRotate(EnumFacing facing) {
        doRotate(getAngleFromRotation(facing));
    }

    static void doRotate(float angle) {
        if (angle != 0) {
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(angle, 0, 1, 0);
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }
    }

    /**
     * Default setup has unrotated models facing south.  Use this if that's the case.
     *
     * @param rotation
     * @return
     */
    static float getAngleFromRotation(EnumFacing rotation) {
        switch (rotation) {
            case NORTH: return 180;
            case EAST: return 90;
            case WEST: return 270;
        }
        return 0;
    }
}
