package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyIOUnit;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderAssemblyIOUnit extends TileEntitySpecialRenderer<TileEntityAssemblyIOUnit> {
    private final ModelAssemblyIOUnit model;

    public RenderAssemblyIOUnit() {
        model = new ModelAssemblyIOUnit();
    }

    @Override
    public void render(TileEntityAssemblyIOUnit tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(
                tile != null && tile.isImportUnit() ? Textures.MODEL_ASSEMBLY_IO_IMPORT : Textures.MODEL_ASSEMBLY_IO_EXPORT);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0, -1.0, -1.0);

        if (tile != null) {
            float[] renderAngles = new float[5];
            for (int i = 0; i < 5; i++) {
                renderAngles[i] = tile.oldAngles[i] + (tile.angles[i] - tile.oldAngles[i]) * partialTicks;
            }

            EntityItem ghostEntityItem = null;
            if (!tile.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                ghostEntityItem = new EntityItem(tile.getWorld());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setItem(tile.getPrimaryInventory().getStackInSlot(0));
            }
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            model.renderModel(0.0625F, renderAngles, tile.oldClawProgress + (tile.clawProgress - tile.oldClawProgress) * partialTicks, ghostEntityItem);
            renderManager.options.fancyGraphics = fancySetting;
        } else {
            model.renderModel(0.0625F, new float[]{0, 0, 35, 55, 0}, 0, null);
        }

        GlStateManager.popMatrix();
    }

}
