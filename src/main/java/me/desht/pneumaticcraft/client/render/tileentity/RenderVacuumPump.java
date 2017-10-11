package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelVacuumPump;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderVacuumPump extends TileEntitySpecialRenderer<TileEntityVacuumPump> {
    private final ModelVacuumPump model;

    public RenderVacuumPump() {
        model = new ModelVacuumPump();
    }

    @Override
    public void render(TileEntityVacuumPump te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_VACUUM_PUMP);

        if (te != null) {
            PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata());
            GlStateManager.rotate(-90, 0, 1, 0);
            model.renderModel(0.0625f, te.oldRotation + (te.rotation - te.oldRotation) * partialTicks);
        } else {
            model.renderModel(0.0625f, 0);
        }

        GlStateManager.popMatrix();
    }
}
