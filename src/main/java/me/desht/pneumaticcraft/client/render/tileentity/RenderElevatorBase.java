package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderElevatorBase extends AbstractModelRenderer<TileEntityElevatorBase> {
    private final ModelElevatorBase model;

    public RenderElevatorBase() {
        model = new ModelElevatorBase();
    }

    @Override
    ResourceLocation getTexture(TileEntityElevatorBase te) {
        return Textures.MODEL_ELEVATOR;
    }

    @Override
    void renderModel(TileEntityElevatorBase te, float partialTicks) {
        model.renderModel(0.0625f, te.oldExtension + (te.extension - te.oldExtension) * partialTicks);
    }
}
