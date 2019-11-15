package me.desht.pneumaticcraft.client.semiblock;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockHeatFrame;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;

public class SemiBlockRendererHeatFrame implements ISemiBlockRenderer<SemiBlockHeatFrame> {
    private final ModelHeatFrame model = new ModelHeatFrame();

    @Override
    public void render(SemiBlockHeatFrame semiBlock, float partialTick) {
        Minecraft.getInstance().getTextureManager().bindTexture(Textures.MODEL_HEAT_FRAME);
        GlStateManager.enableTexture();
        int heatLevel = semiBlock.getHeatLevel();
        float[] color = HeatUtil.getColorForHeatLevel(heatLevel);
        float lightMul = getLightMultiplier(semiBlock);
        GlStateManager.color4f(color[0] * lightMul, color[1] * lightMul, color[2] * lightMul, 1);
        AxisAlignedBB aabb = getBounds(semiBlock);
        GlStateManager.translated(aabb.minX, aabb.minY, aabb.minZ);
        GlStateManager.scaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GlStateManager.translated(0.5, -0.5, 0.5);
        model.render(1 / 16F);
        GlStateManager.color4f(1, 1, 1, 1);
    }

    private float getLightMultiplier(SemiBlockHeatFrame semiBlock) {
        if (!PNCConfig.Client.semiBlockLighting) return 1.0F;

        float lightMul = Math.max(0.05F, Minecraft.getInstance().world.getLight(semiBlock.getPos()) / 15F);
        if (semiBlock.getHeatLevel() > 15) {
            lightMul = Math.min(6.0F, lightMul + (semiBlock.getHeatLevel() - 15) / 3F);
        } else if (semiBlock.getHeatLevel() > 11) {
            lightMul = lightMul + 0.07F * (semiBlock.getHeatLevel() - 11);
        }
        return lightMul;
    }
}
