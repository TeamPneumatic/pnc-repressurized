package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelElevatorBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderElevatorBase extends AbstractModelRenderer<TileEntityElevatorBase> {
    private final ModelElevatorBase model;

    public RenderElevatorBase() {
        model = new ModelElevatorBase();
    }

    @Override
    protected boolean shouldRender(TileEntityElevatorBase te) {
        return te.extension > 0;
    }

    @Override
    ResourceLocation getTexture(TileEntityElevatorBase te) {
        return Textures.MODEL_ELEVATOR;
    }

    @Override
    void renderModel(TileEntityElevatorBase te, float partialTicks) {
        float extension = te.oldExtension + (te.extension - te.oldExtension) * partialTicks;
        model.renderModel(0.0625f, extension);
    }

    @Override
    protected void renderExtras(TileEntityElevatorBase te, double x, double y, double z, float partialTicks) {
        if (te.fakeFloorTextureUV != null && te.fakeFloorTextureUV.length == 4) {
            // draw the fake elevator floor texture
            GlStateManager.pushMatrix();
            float extension = te.oldExtension + (te.extension - te.oldExtension) * partialTicks;
            GlStateManager.translate(x, y + extension + 1.0005f, z);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            double uMin = te.fakeFloorTextureUV[0];
            double vMin = te.fakeFloorTextureUV[1];
            double uMax = te.fakeFloorTextureUV[2];
            double vMax = te.fakeFloorTextureUV[3];
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder worldrenderer = tessellator.getBuffer();
            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(0, 0, 1).tex(uMin, vMax).endVertex();
            worldrenderer.pos(1, 0, 1).tex(uMax, vMax).endVertex();
            worldrenderer.pos(1, 0, 0).tex(uMax, vMin).endVertex();
            worldrenderer.pos(0, 0, 0).tex(uMin, vMin).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityElevatorBase te) {
        // since we can be very tall...
        return true;
    }
}
