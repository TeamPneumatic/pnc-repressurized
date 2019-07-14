package me.desht.pneumaticcraft.client.model.module;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

public abstract class ModelModuleBase extends Model {
    protected final void setRotation(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public final void renderModel(float scale, TubeModule module, float partialTicks) {
        GlStateManager.pushMatrix();

        Minecraft.getInstance().getTextureManager().bindTexture(getTexture());
        RenderUtils.rotateMatrixByMetadata(module.getDirection());
        renderDynamic(scale, partialTicks);

        GlStateManager.popMatrix();
    }

    protected abstract void renderDynamic(float scale, float partialTicks);

    protected abstract ResourceLocation getTexture();

    // used if there's any kind of problem instantiating the actual model for the module
    public static class MissingModel extends ModelModuleBase {
        @Override
        protected void renderDynamic(float scale, float partialTicks) {
        }

        @Override
        protected ResourceLocation getTexture() {
            return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
        }
    }
}
