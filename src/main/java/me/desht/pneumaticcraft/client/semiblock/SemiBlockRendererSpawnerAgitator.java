package me.desht.pneumaticcraft.client.semiblock;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockSpawnerAgitator;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;

public class SemiBlockRendererSpawnerAgitator implements ISemiBlockRenderer<SemiBlockSpawnerAgitator> {
    private final ModelHeatFrame model = new ModelHeatFrame();

    @Override
    public void render(SemiBlockSpawnerAgitator semiBlock, float partialTick) {
        GlStateManager.pushMatrix();
        Minecraft.getInstance().getTextureManager().bindTexture(Textures.MODEL_HEAT_FRAME);

        float brightness = 0.2F;
        GlStateManager.color4f(brightness, brightness, brightness, 1);

        AxisAlignedBB aabb = getBounds(semiBlock);
        GlStateManager.translated(aabb.minX, aabb.minY, aabb.minZ);
        GlStateManager.scaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GlStateManager.translated(0.5, -0.5, 0.5);
        model.render(1 / 16F);
        GlStateManager.popMatrix();
        GlStateManager.color4f(1, 1, 1, 1);
    }
}
