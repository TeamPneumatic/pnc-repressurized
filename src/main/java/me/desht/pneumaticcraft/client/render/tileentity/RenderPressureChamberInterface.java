package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.block.ModelPressureChamberInterface;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.util.ResourceLocation;

public class RenderPressureChamberInterface extends AbstractTileModelRenderer<TileEntityPressureChamberInterface> {
    private final ModelPressureChamberInterface model;

    public RenderPressureChamberInterface() {
        model = new ModelPressureChamberInterface();
    }

    @Override
    ResourceLocation getTexture(TileEntityPressureChamberInterface te) {
        return Textures.MODEL_PRESSURE_CHAMBER_INTERFACE;
    }

    @Override
    void renderModel(TileEntityPressureChamberInterface te, float partialTicks) {
        if (te != null) {
            RenderUtils.rotateMatrixForDirection(te.getRotation());
            model.renderModel(0.0625f, te, partialTicks);
            GlStateManager.translated(0, 1f, 0.25F);
            GlStateManager.scaled(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer().renderItem(te.getStackInInterface(), ItemCameraTransforms.TransformType.FIXED);
        }
    }
}
