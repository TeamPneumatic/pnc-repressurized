package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;

public class SemiBlockRendererLogistics implements ISemiBlockRenderer<SemiBlockLogistics> {
    private static final double FRAME_WIDTH = 1 / 32D;
    private static final AxisAlignedBB DEFAULT_BOX = new AxisAlignedBB(
            0 + FRAME_WIDTH, 0 + FRAME_WIDTH, 0 + FRAME_WIDTH,
            1 - FRAME_WIDTH, 1 - FRAME_WIDTH, 1 - FRAME_WIDTH
    );

    @Override
    public void render(SemiBlockLogistics semiBlock, float partialTick) {
        int alpha = semiBlock.getAlpha();
        if (alpha == 0) return;
        if (alpha < 255) GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        RenderUtils.glColorHex((alpha << 24 | 0x00FFFFFF) & semiBlock.getColor(), getLightMultiplier(semiBlock));
        AxisAlignedBB aabb = semiBlock.getWorld() != null ?
                semiBlock.getBlockState().getBoundingBox(semiBlock.getWorld(), semiBlock.getPos()) : DEFAULT_BOX;
        RenderUtils.renderFrame(aabb, FRAME_WIDTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    private float getLightMultiplier(ISemiBlock semiBlock) {
        return Math.max(1, Minecraft.getMinecraft().world.getLight(semiBlock.getPos())) / 15F;
    }

}
