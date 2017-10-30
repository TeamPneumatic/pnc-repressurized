package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;

public class RenderThermopneumaticProcessingPlant extends TileEntitySpecialRenderer<TileEntityThermopneumaticProcessingPlant> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(1 / 16f, 1 / 16f, 10 / 16f, 6 / 16f, 11 / 16f, 15 / 16f);

    @Override
    public void render(TileEntityThermopneumaticProcessingPlant te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getInputTank().getFluidAmount() == 0 && te.getOutputTank().getFluidAmount() == 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // unrotated: input tank at SW corner, output tank at SE corner
        TankRenderHelper.doRotate(te.getRotation());

        FluidTank inputTank = te.getInputTank();
        if (inputTank.getFluidAmount() > 0) {
            AxisAlignedBB bounds = getRenderBounds(inputTank);
            PneumaticCraftUtils.renderFluid(inputTank.getFluid().getFluid(), bounds);
        }

        FluidTank outputTank = te.getOutputTank();
        if (outputTank.getFluidAmount() > 0) {
            AxisAlignedBB bounds = getRenderBounds(outputTank);
            TankRenderHelper.doRotate(90);
            PneumaticCraftUtils.renderFluid(outputTank.getFluid().getFluid(), bounds);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private AxisAlignedBB getRenderBounds(FluidTank tank) {
        return TankRenderHelper.getRenderBounds(tank, TANK_BOUNDS);
    }
}
