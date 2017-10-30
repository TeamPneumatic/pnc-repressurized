package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.IFluidTank;
import org.lwjgl.opengl.GL11;

public class RenderPlasticMixer extends TileEntitySpecialRenderer<TileEntityPlasticMixer> {
    private static final AxisAlignedBB TANK_BOUNDS = new AxisAlignedBB(2.5/16f, 1/16f, 2.5/16f, 13.5/16f, 15/16f, 13.5/16f);
    @Override
    public void render(TileEntityPlasticMixer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IFluidTank tank = te.getTank();
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

    private AxisAlignedBB getRenderBounds(IFluidTank tank) {
        return TankRenderHelper.getRenderBounds(tank, TANK_BOUNDS);
    }
}
