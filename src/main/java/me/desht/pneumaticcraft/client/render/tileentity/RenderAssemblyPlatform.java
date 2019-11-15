package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyPlatform;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.ResourceLocation;

public class RenderAssemblyPlatform extends AbstractTileModelRenderer<TileEntityAssemblyPlatform> {
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
            ItemEntity ghostEntityItem = null;
            if (!te.getHeldStack().isEmpty()) {
                ghostEntityItem = new ItemEntity(EntityType.ITEM, te.getWorld());
//                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setItem(te.getHeldStack());
            }
            EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            model.renderModel(0.0625f, te.oldClawProgress + (te.clawProgress - te.oldClawProgress) * partialTicks, ghostEntityItem);
            renderManager.options.fancyGraphics = fancySetting;
        } else {
            model.renderModel(0.0625f, 0, null);
        }
    }
}
