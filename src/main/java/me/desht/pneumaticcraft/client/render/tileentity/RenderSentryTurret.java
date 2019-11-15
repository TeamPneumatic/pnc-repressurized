package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelSentryTurret;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderSentryTurret extends AbstractTileModelRenderer<TileEntitySentryTurret> {
    private final ModelSentryTurret model;

    public RenderSentryTurret() {
        model = new ModelSentryTurret();
    }

    @Override
    ResourceLocation getTexture(TileEntitySentryTurret te) {
        return Textures.MODEL_SENTRY_TURRET;
    }

    @Override
    void renderModel(TileEntitySentryTurret te, float partialTicks) {
        model.renderModel(0.0625f, te, partialTicks);
    }
}
