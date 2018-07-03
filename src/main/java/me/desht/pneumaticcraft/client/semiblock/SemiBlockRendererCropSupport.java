package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.model.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockCropSupport;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;

public class SemiBlockRendererCropSupport implements ISemiBlockRenderer<SemiBlockCropSupport> {
    private final ModelCropSupport model = new ModelCropSupport();

    @Override
    public void render(SemiBlockCropSupport semiBlock, float partialTick) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(Textures.MODEL_HEAT_FRAME);

        float brightness = 0.2F;
        GlStateManager.color(brightness, brightness, brightness, 1F);

        AxisAlignedBB aabb = new AxisAlignedBB(3 / 16D, -6 / 16D, 3 / 16D, 13 / 16D, 17 / 16D, 13 / 16D);
       
        GlStateManager.translate(aabb.minX, aabb.minY, aabb.minZ);
        GlStateManager.scale(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GlStateManager.translate(0.5, -0.5, 0.5);
        model.render(null, 0, 0, 0, 0, 0, 1 / 16F);
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }
}
