package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyPlatform;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderAssemblyPlatform extends TileEntitySpecialRenderer<TileEntityAssemblyPlatform> {
    private final ModelAssemblyPlatform model;

    public RenderAssemblyPlatform() {
        model = new ModelAssemblyPlatform();
    }

    @Override
    public void render(TileEntityAssemblyPlatform te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_ASSEMBLY_PLATFORM);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

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

        GlStateManager.popMatrix();
    }
}
