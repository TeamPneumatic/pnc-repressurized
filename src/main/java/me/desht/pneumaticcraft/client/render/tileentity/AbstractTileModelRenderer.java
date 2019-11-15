package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;

public abstract class AbstractTileModelRenderer<T extends TileEntityBase> extends TileEntityRenderer<T> {
    abstract ResourceLocation getTexture(T te);

    abstract void renderModel(T te, float partialTicks);

    protected boolean shouldRender(T te) {
        return true;
    }

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage) {
        // boilerplate translation code, common to all TESRs, done here

        if (!shouldRender(te) || !te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;

        GlStateManager.pushMatrix();
        Minecraft.getInstance().getTextureManager().bindTexture(getTexture(te));
        GlStateManager.translated((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F); // size
        GlStateManager.scaled(1.0F, -1F, -1F); // to make your block have a normal positioning. comment out to see what happens

        // actual model rendering work
        renderModel(te, partialTicks);

        GlStateManager.popMatrix();
    }

    public static abstract class BaseModel extends Model {
        public void setRotation(RendererModel model, float x, float y, float z){
            model.rotateAngleX = x;
            model.rotateAngleY = y;
            model.rotateAngleZ = z;
        }
    }

    public static class NoBobItemRenderer extends ItemRenderer {
        public NoBobItemRenderer() {
            super(Minecraft.getInstance().getRenderManager(), Minecraft.getInstance().getItemRenderer());
        }

        @Override
        public boolean shouldBob() {
            return false;
        }
    }
}
