package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelUniversalSensor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderUniversalSensor extends TileEntitySpecialRenderer<TileEntityUniversalSensor> {
    private final ModelUniversalSensor model;

    public RenderUniversalSensor() {
        model = new ModelUniversalSensor();
    }

    @Override
    public void render(TileEntityUniversalSensor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_UNIVERSAL_SENSOR);

        if (te != null) {
            model.renderModel(0.0625f, te.oldDishRotation + (te.dishRotation - te.oldDishRotation) * partialTicks);
            te.renderRangeLines();
        }

        GlStateManager.popMatrix();
    }
}
