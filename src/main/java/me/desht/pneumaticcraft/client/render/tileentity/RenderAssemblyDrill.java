package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyDrill;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyDrill;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderAssemblyDrill extends TileEntitySpecialRenderer<TileEntityAssemblyDrill> {
    private final ModelAssemblyDrill model;

    public RenderAssemblyDrill() {
        model = new ModelAssemblyDrill();
    }

    @Override
    public void render(TileEntityAssemblyDrill te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_ASSEMBLY_LASER_AND_DRILL);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        if (te != null) {
            float[] renderAngles = new float[5];
            for (int i = 0; i < 4; i++) {
                renderAngles[i] = te.oldAngles[i] + (te.angles[i] - te.oldAngles[i]) * partialTicks;
            }
            renderAngles[4] = te.oldDrillRotation + (te.drillRotation - te.oldDrillRotation) * partialTicks;
            model.renderModel(0.0625f, renderAngles);
        } else {
            model.renderModel(0.0625f, new float[]{0, 0, 35, 55, 0});
        }

        GlStateManager.popMatrix();
    }
}
