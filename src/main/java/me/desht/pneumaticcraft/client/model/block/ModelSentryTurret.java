package me.desht.pneumaticcraft.client.model.block;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.entity.ModelDroneMinigun;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractTileModelRenderer;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class ModelSentryTurret extends AbstractTileModelRenderer.BaseModel {
    private final ModelDroneMinigun model = new ModelDroneMinigun();
    private TileEntitySentryTurret fakeTurret;

    public ModelSentryTurret() {
    }

    public void renderModel(float scale, TileEntitySentryTurret te, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(0, -13 / 16D, 0);
        if (te == null) {
            if (fakeTurret == null) {
                fakeTurret = new TileEntitySentryTurret();
            }
            model.renderMinigun(fakeTurret.getMinigun(), 1 / 16F, partialTicks, false);
        } else {
            model.renderMinigun(te.getMinigun(), 1 / 16F, partialTicks, false);
            GlStateManager.pushMatrix();
            GlStateManager.scaled(1.0F, -1, -1F);
            GlStateManager.translated(0, -1.45F, 0);
            BlockPos pos = te.getPos();
            RenderMinigunTracers.instance().render(te.getMinigun(),pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        Minecraft.getInstance().getTextureManager().bindTexture(Textures.MODEL_SENTRY_TURRET);
    }
}
