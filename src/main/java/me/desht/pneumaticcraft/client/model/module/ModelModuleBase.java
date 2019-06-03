package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public abstract class ModelModuleBase extends ModelBase {
    protected final void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public final void renderModel(float scale, TubeModule module, float partialTicks) {
        GlStateManager.pushMatrix();

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
        RenderUtils.rotateMatrixByMetadata(module.getDirection().ordinal());
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
            return TextureMap.LOCATION_BLOCKS_TEXTURE;
        }
    }
}
