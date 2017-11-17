package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelUniversalSensor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderUniversalSensor extends AbstractModelRenderer<TileEntityUniversalSensor> {
    private final ModelUniversalSensor model;

    public RenderUniversalSensor() {
        model = new ModelUniversalSensor();
    }

    @Override
    ResourceLocation getTexture(TileEntityUniversalSensor te) {
        return Textures.MODEL_UNIVERSAL_SENSOR;
    }

    @Override
    void renderModel(TileEntityUniversalSensor te, float partialTicks) {
        if (te != null) {
            model.renderModel(0.0625f, te.oldDishRotation + (te.dishRotation - te.oldDishRotation) * partialTicks);
            te.renderRangeLines();
        }
    }
}
