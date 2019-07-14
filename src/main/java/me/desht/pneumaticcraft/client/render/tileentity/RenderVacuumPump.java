package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.block.ModelVacuumPump;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderVacuumPump extends AbstractModelRenderer<TileEntityVacuumPump> {
    private final ModelVacuumPump model;

    public RenderVacuumPump() {
        model = new ModelVacuumPump();
    }

    @Override
    ResourceLocation getTexture(TileEntityVacuumPump te) {
        return Textures.MODEL_VACUUM_PUMP;
    }

    @Override
    void renderModel(TileEntityVacuumPump te, float partialTicks) {
        if (te != null) {
            RenderUtils.rotateMatrixByMetadata(te.getRotation());
            GlStateManager.rotated(-90, 0, 1, 0);
            model.renderModel(0.0625f, te.oldRotation + (te.rotation - te.oldRotation) * partialTicks);
        } else {
            model.renderModel(0.0625f, 0);
        }
    }
}
