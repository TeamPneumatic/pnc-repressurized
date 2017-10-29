package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;

public class RenderLiquidHopper extends TileEntitySpecialRenderer<TileEntityLiquidHopper> {
    @Override
    public void render(TileEntityLiquidHopper te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FluidTank tank = te.getTank();
        if (tank.getFluidAmount() == 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        doRotate(te.getInputDirection());

        AxisAlignedBB bounds = getRenderBounds(te.getInputDirection(), tank);
        PneumaticCraftUtils.renderFluid(tank.getFluid().getFluid(), bounds);

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private void doRotate(EnumFacing rotation) {
        // no need to do a rotation when facing up or down
        if (rotation.getAxis().isHorizontal()) {
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(getAngleFromRotation(rotation), 0, 1, 0);
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }
    }

    private float getAngleFromRotation(EnumFacing rotation) {
        switch (rotation) {
            case NORTH: return 180;
            case EAST: return 90;
            case WEST: return 270;
        }
        return 0;
    }

    private AxisAlignedBB getRenderBounds(EnumFacing rotation, FluidTank tank) {
        float percent =  (float) tank.getFluidAmount() / (float) tank.getCapacity();
        switch (rotation) {
            case UP:
                return new AxisAlignedBB(1 / 16f, 11 / 16f, 1 / 16f, 15 / 16f, (11 + 4 * percent) / 16, 15 / 16f);
            case DOWN:
                return new AxisAlignedBB(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, (1 + 4 * percent) / 16, 15 / 16f);
            default:
                return new AxisAlignedBB(1 / 16f, 1 / 16f, 11 / 16f, 15 / 16f, (1 + 14 * percent) / 16, 15 / 16f);
        }
    }
}
