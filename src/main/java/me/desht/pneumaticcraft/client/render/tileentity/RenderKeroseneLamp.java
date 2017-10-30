package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;

public class RenderKeroseneLamp extends TileEntitySpecialRenderer<TileEntityKeroseneLamp> {
    @Override
    public void render(TileEntityKeroseneLamp te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FluidTank tank = te.getTank();
        if (tank.getFluidAmount() == 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        AxisAlignedBB bounds = getRenderBounds(tank);
        PneumaticCraftUtils.renderFluid(tank.getFluid().getFluid(), bounds);

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private AxisAlignedBB getRenderBounds(FluidTank tank) {
        float percent =  (float) tank.getFluidAmount() / (float) tank.getCapacity();
        return new AxisAlignedBB(6 /16f, 1 / 16f, 6 / 16f, 10/ 16f, (1 + (8 * percent)) / 16f, 10 / 16f);
    }
}
