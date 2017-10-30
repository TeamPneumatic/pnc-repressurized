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
    private static final AxisAlignedBB TANK_BOUNDS_UP = new AxisAlignedBB(1 / 16f, 11 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);
    private static final AxisAlignedBB TANK_BOUNDS_DOWN = new AxisAlignedBB(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 5 / 16f, 15 / 16f);
    private static final AxisAlignedBB TANK_BOUNDS_HORIZ = new AxisAlignedBB(1 / 16f, 1 / 16f, 11 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);

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
        if (rotation.getAxis().isHorizontal()) {
            // no need to do a rotation when facing up or down
            TankRenderHelper.doRotate(rotation);
        }
    }

    private AxisAlignedBB getRenderBounds(EnumFacing rotation, FluidTank tank) {
        switch (rotation) {
            case UP: return TankRenderHelper.getRenderBounds(tank, TANK_BOUNDS_UP);
            case DOWN: return TankRenderHelper.getRenderBounds(tank, TANK_BOUNDS_DOWN);
            default: return TankRenderHelper.getRenderBounds(tank, TANK_BOUNDS_HORIZ);
        }
    }
}
