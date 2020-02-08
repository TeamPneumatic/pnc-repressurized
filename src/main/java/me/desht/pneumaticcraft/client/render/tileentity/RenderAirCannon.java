package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAirCannon;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderAirCannon extends AbstractTileModelRenderer<TileEntityAirCannon> {
    private final ModelAirCannon model;

    public RenderAirCannon() {
        model = new ModelAirCannon();
    }

    @Override
    ResourceLocation getTexture(TileEntityAirCannon te) {
        return Textures.MODEL_AIR_CANNON;
    }

    @Override
    void renderModel(TileEntityAirCannon te, float partialTicks) {
        float angle = (float) RenderUtils.rotateMatrixForDirection(te.getRotation());
        float rotationAngle = te.rotationAngle - angle + 180F;
        model.renderModel(0.0625F, rotationAngle, te.heightAngle);
    }
}
