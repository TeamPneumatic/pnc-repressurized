package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyLaser;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyLaser;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderAssemblyLaser extends TileEntitySpecialRenderer<TileEntityAssemblyLaser> {
    private final ModelAssemblyLaser model;

    public RenderAssemblyLaser() {
        model = new ModelAssemblyLaser();
    }

    @Override
    public void render(TileEntityAssemblyLaser te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_ASSEMBLY_LASER_AND_DRILL);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        if (te != null) {
            float[] renderAngles = new float[5];
            for (int i = 0; i < 5; i++) {
                renderAngles[i] = te.oldAngles[i] + (te.angles[i] - te.oldAngles[i]) * partialTicks;
            }
            model.renderModel(0.0625F, renderAngles, te.isLaserOn);
        } else {
            model.renderModel(0.0625F, new float[]{0, 0, 35, 55, 0}, false);
        }

        GlStateManager.popMatrix();
    }
}
