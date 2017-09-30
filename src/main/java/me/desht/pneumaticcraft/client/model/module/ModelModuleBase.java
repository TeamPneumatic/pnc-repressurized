package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public abstract class ModelModuleBase extends ModelBase {
    protected final void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public final void renderModel(float scale, EnumFacing dir, float partialTicks) {
        GlStateManager.pushMatrix();

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
        PneumaticCraftUtils.rotateMatrixByMetadata(dir.ordinal());
        renderDynamic(scale, partialTicks);

        GlStateManager.popMatrix();
    }

    protected abstract void renderDynamic(float scale, float partialTicks);

    protected abstract ResourceLocation getTexture();
}
