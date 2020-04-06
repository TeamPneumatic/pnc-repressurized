package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderElevatorBase extends AbstractModelRenderer<TileEntityElevatorBase> {
    private final ModelElevatorBase model;

    public RenderElevatorBase() {
        model = new ModelElevatorBase();
    }

    @Override
    protected boolean shouldRender(TileEntityElevatorBase te) {
        return te.extension > 0;
    }

    @Override
    ResourceLocation getTexture(TileEntityElevatorBase te) {
        return Textures.MODEL_ELEVATOR;
    }

    @Override
    void renderModel(TileEntityElevatorBase te, float partialTicks) {
        model.renderModel(0.0625f, te.oldExtension + (te.extension - te.oldExtension) * partialTicks);
    }

    @Override
    public boolean isGlobalRenderer(TileEntityElevatorBase te) {
        // since we can be very tall...
        return true;
    }
}
