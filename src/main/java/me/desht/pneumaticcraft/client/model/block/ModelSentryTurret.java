package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.model.entity.ModelDroneMinigun;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

public class ModelSentryTurret extends AbstractModelRenderer.BaseModel {
    private final ModelDroneMinigun model = new ModelDroneMinigun();
    private final TileEntitySentryTurret fakeTurret = new TileEntitySentryTurret();

    public ModelSentryTurret() {
    }

    public void renderModel(float scale, TileEntitySentryTurret te, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -13 / 16D, 0);
        if (te == null) {
            model.renderMinigun(fakeTurret.getMinigun(), 1 / 16F, partialTicks, false);
        } else {
            model.renderMinigun(te.getMinigun(), 1 / 16F, partialTicks, false);
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F, -1, -1F);
            GlStateManager.translate(0, -1.45F, 0);
            BlockPos pos = te.getPos();
            te.getMinigun().render(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(Textures.MODEL_SENTRY_TURRET);
    }
}
