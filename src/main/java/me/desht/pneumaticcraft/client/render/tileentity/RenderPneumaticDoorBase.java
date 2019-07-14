package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelDoorBase;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderPneumaticDoorBase extends AbstractModelRenderer<TileEntityPneumaticDoorBase> {
    private final ModelDoorBase model;

    public RenderPneumaticDoorBase() {
        model = new ModelDoorBase();
    }

    @Override
    ResourceLocation getTexture(TileEntityPneumaticDoorBase te) {
        return Textures.MODEL_PNEUMATIC_DOOR_BASE;
    }

    @Override
    void renderModel(TileEntityPneumaticDoorBase te, float partialTicks) {
        if (te != null) {
            RenderUtils.rotateMatrixByMetadata(te.getRotation());
            model.renderModel(0.0625f, te.oldProgress + (te.progress - te.oldProgress) * partialTicks, te.rightGoing);
        } else {
            model.renderModel(0.0625f, 1, false);
        }
    }
}
