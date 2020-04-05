package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;

public abstract class AbstractTileModelRenderer<T extends TileEntityBase> extends TileEntityRenderer<T> {
    public AbstractTileModelRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    abstract ResourceLocation getTexture(T te);

    abstract void renderModel(T te, MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, int combinedLightmap, int combinedOverlay);

    protected boolean shouldRender(T te) {
        return true;
    }

    @Override
    public void render(T te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int combinedLightMap, int combinedOverlay) {
        // boilerplate translation code, common to all TESRs, done here

        if (!shouldRender(te) || !te.getWorld().getChunkProvider().isChunkLoaded(new ChunkPos(te.getPos()))) return;

//        GlStateManager.pushMatrix();
//        GlStateManager.translated((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F); // size
//        GlStateManager.scaled(1.0F, -1F, -1F); // to make your block have a normal positioning. comment out to see what happens
        Minecraft.getInstance().getTextureManager().bindTexture(getTexture(te));

        // actual model rendering work
        matrixStack.push();
        renderModel(te, matrixStack, iRenderTypeBuffer, partialTicks, combinedLightMap, combinedOverlay);
        matrixStack.pop();

//        GlStateManager.popMatrix();
    }

    public static abstract class BaseModel extends EntityModel {
        public void setRotation(ModelRenderer model, float x, float y, float z){
            model.rotateAngleX = x;
            model.rotateAngleY = y;
            model.rotateAngleZ = z;
        }
    }
}
