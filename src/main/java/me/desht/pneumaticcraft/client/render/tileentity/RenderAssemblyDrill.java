package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyDrill;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyDrill;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderAssemblyDrill extends AbstractTileModelRenderer<TileEntityAssemblyDrill> {
    private final ModelAssemblyDrill model;

    public RenderAssemblyDrill() {
        model = new ModelAssemblyDrill();
    }

    @Override
    ResourceLocation getTexture(TileEntityAssemblyDrill te) {
        return Textures.MODEL_ASSEMBLY_LASER_AND_DRILL;
    }

    @Override
    void renderModel(TileEntityAssemblyDrill te, float partialTicks) {
        if (te != null) {
            float[] renderAngles = new float[5];
            for (int i = 0; i < 4; i++) {
                renderAngles[i] = MathHelper.lerp(partialTicks, te.oldAngles[i], te.angles[i]);
            }
            renderAngles[4] = te.oldDrillRotation + (te.drillRotation - te.oldDrillRotation) * partialTicks;
            model.renderModel(0.0625f, renderAngles);
        } else {
            model.renderModel(0.0625f, new float[]{0, 0, 35, 55, 0});
        }
    }
}
