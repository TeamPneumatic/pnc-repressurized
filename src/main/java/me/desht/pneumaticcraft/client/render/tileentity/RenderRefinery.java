package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import org.lwjgl.opengl.GL11;

public class RenderRefinery extends TileEntitySpecialRenderer<TileEntityRefinery> {
    @Override
    public void render(TileEntityRefinery te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getOilTank().getFluidAmount() == 0 && te.getOutputTank().getFluidAmount() == 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // note: unrotated model has oil tank on the south face, output tank on the north face

        doRotate(getAngleFromRotation(te.getRotation()));

        FluidTank oilTank = te.getOilTank();
        if (oilTank.getFluidAmount() > 0) {
            AxisAlignedBB bounds = getRenderBounds(oilTank);
            PneumaticCraftUtils.renderFluid(oilTank.getFluid().getFluid(), bounds);
        }

        FluidTank outputTank = te.getOutputTank();
        if (outputTank.getFluidAmount() > 0) {
            AxisAlignedBB bounds = getRenderBounds(outputTank);
            doRotate(180);
            PneumaticCraftUtils.renderFluid(outputTank.getFluid().getFluid(), bounds);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private void doRotate(float angle) {
        if (angle != 0) {
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(angle, 0, 1, 0);
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }
    }

    private AxisAlignedBB getRenderBounds(IFluidTank tank) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();
        return new AxisAlignedBB(4.25f/16f, 1f/16f, 0.25f/16f, 11.75f/16f, (1f + 14f * percent)/16f, 3f/16f);
    }

    private float getAngleFromRotation(EnumFacing rotation) {
        switch (rotation) {
            case NORTH: return 180;
            case EAST: return 90;
            case WEST: return 270;
        }
        return 0;
    }
}
