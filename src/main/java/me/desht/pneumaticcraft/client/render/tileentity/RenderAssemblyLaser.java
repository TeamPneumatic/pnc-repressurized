package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyLaser;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyLaser;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderAssemblyLaser extends AbstractModelRenderer<TileEntityAssemblyLaser> {
    private final ModelAssemblyLaser model;

    public RenderAssemblyLaser() {
        model = new ModelAssemblyLaser();
    }

    @Override
    ResourceLocation getTexture(TileEntityAssemblyLaser te) {
        return Textures.MODEL_ASSEMBLY_LASER_AND_DRILL;
    }

    @Override
    void renderModel(TileEntityAssemblyLaser te, float partialTicks) {
        if (te != null) {
            float[] renderAngles = new float[5];
            for (int i = 0; i < 5; i++) {
                renderAngles[i] = te.oldAngles[i] + (te.angles[i] - te.oldAngles[i]) * partialTicks;
            }
            model.renderModel(0.0625F, renderAngles, te.isLaserOn);
        } else {
            model.renderModel(0.0625F, new float[]{0, 0, 35, 55, 0}, false);
        }
    }
}
