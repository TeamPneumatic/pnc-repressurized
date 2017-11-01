package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelSentryTurret;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderSentryTurret extends TileEntitySpecialRenderer<TileEntitySentryTurret> {
    private final ModelSentryTurret model;

    public RenderSentryTurret() {
        model = new ModelSentryTurret();
    }

    @Override
    public void render(TileEntitySentryTurret te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_SENTRY_TURRET);
        model.renderModel(0.0625f, te, partialTicks);

        GlStateManager.popMatrix();
    }
}
