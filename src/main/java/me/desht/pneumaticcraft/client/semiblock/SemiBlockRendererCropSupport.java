package me.desht.pneumaticcraft.client.semiblock;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockCropSupport;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;

public class SemiBlockRendererCropSupport implements ISemiBlockRenderer<SemiBlockCropSupport> {
    private final ModelCropSupport model = new ModelCropSupport();

    @Override
    public void render(SemiBlockCropSupport semiBlock, float partialTick) {
        GlStateManager.pushMatrix();
        Minecraft.getInstance().getTextureManager().bindTexture(Textures.MODEL_HEAT_FRAME);

        float brightness = 0.2F;
        GlStateManager.color4f(brightness, brightness, brightness, 1F);

        AxisAlignedBB aabb = new AxisAlignedBB(3 / 16D, -6 / 16D, 3 / 16D, 13 / 16D, 17 / 16D, 13 / 16D);
       
        GlStateManager.translated(aabb.minX, aabb.minY, aabb.minZ);
        GlStateManager.scaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GlStateManager.translated(0.5, -0.5, 0.5);
        model.render(1 / 16F);
        GlStateManager.popMatrix();
        GlStateManager.color4f(1, 1, 1, 1);
    }
}
