package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityTickableBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public abstract class AbstractModelRenderer<T extends TileEntityTickableBase> extends TileEntitySpecialRenderer<T> {
    abstract ResourceLocation getTexture(T te);

    abstract void renderModel(T te, float partialTicks);

    protected boolean shouldRender(T te) {
        return true;
    }

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // boilerplate translation code, common to all model renders, done here

        if (!shouldRender(te)) return;

        GlStateManager.pushMatrix();
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture(te));
        GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F); // size
        GlStateManager.scale(1.0F, -1F, -1F); // to make your block have a normal positioning. comment out to see what happens

        // actual model rendering work
        renderModel(te, partialTicks);

        GlStateManager.popMatrix();
    }

    public static abstract class BaseModel extends ModelBase {
        public void setRotation(ModelRenderer model, float x, float y, float z){
            model.rotateAngleX = x;
            model.rotateAngleY = y;
            model.rotateAngleZ = z;
        }
    }

    public static class NoBobItemRenderer extends RenderEntityItem {
        public NoBobItemRenderer() {
            super(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem());
        }

        @Override
        public boolean shouldBob() {
            return false;
        }
    }
}
