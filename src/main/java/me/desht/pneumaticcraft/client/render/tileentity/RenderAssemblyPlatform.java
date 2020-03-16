package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAssemblyPlatform;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

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
            EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            model.renderModel(0.0625f, MathHelper.lerp(partialTicks, te.oldClawProgress, te.clawProgress),  te.getPrimaryInventory().getStackInSlot(0));
            renderManager.options.fancyGraphics = fancySetting;
        } else {
            model.renderModel(0.0625f, 0, ItemStack.EMPTY);
        }
    }
}
