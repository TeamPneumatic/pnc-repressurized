package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderElevatorBase extends AbstractTileModelRenderer<TileEntityElevatorBase> {
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
//        int light = te.getWorld().getCombinedLight(te.getPos().up(), 0);  // otherwise it will render unlit
//        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) (light & 0xFFFF), (float) ((light >> 16) & 0xFFFF));
        model.renderModel(0.0625f, te.oldExtension + (te.extension - te.oldExtension) * partialTicks);
    }
}
