package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyPlatform;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

public class RenderAssemblyPlatform extends AbstractModelRenderer<TileEntityAssemblyPlatform> {
    private final ModelAssemblyPlatform model;

    public RenderAssemblyPlatform() {
        model = new ModelAssemblyPlatform();
    }

    @Override
    ResourceLocation getTexture(TileEntityAssemblyPlatform te) {
        return Textures.MODEL_ASSEMBLY_PLATFORM;
    }

    @Override
    void renderModel(TileEntityAssemblyPlatform te, float partialTicks) {
        if (te != null) {
            EntityItem ghostEntityItem = null;
            if (!te.getHeldStack().isEmpty()) {
                ghostEntityItem = new EntityItem(te.getWorld());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setItem(te.getHeldStack());
            }
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            model.renderModel(0.0625f, te.oldClawProgress + (te.clawProgress - te.oldClawProgress) * partialTicks, ghostEntityItem);
            renderManager.options.fancyGraphics = fancySetting;
        } else {
            model.renderModel(0.0625f, 0, null);
        }
    }
}
