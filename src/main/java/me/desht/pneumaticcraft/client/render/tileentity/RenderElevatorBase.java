package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderElevatorBase extends TileEntitySpecialRenderer<TileEntityElevatorBase> {
    private final ModelElevatorBase model;

    public RenderElevatorBase() {
        model = new ModelElevatorBase();
    }

    @Override
    public void render(TileEntityElevatorBase te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.extension == 0) return;

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_ELEVATOR);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        model.renderModel(0.0625f, te.oldExtension + (te.extension - te.oldExtension) * partialTicks);

        GlStateManager.popMatrix();
    }
}
