package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import static net.minecraft.util.math.MathHelper.lerp;

public class RenderRing extends ProgressingLine {
    private final int color;

    public RenderRing(double startX, double startY, double startZ, double endX, double endY, double endZ, int color) {
        super(startX, startY, startZ, endX, endY, endZ);
        this.color = color;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderInterpolated(ProgressingLine lastTickLine, float partialTick, float rotationYaw, float rotationPitch) {
        GlStateManager.pushMatrix();
        double renderProgress = lerp(partialTick, progress, lastTickLine.progress);
        GlStateManager.translated((lerp(partialTick, endX, lastTickLine.endX) - startX) * renderProgress, (lerp(partialTick, endY, lastTickLine.endY) - startY) * renderProgress, (lerp(partialTick, endZ, lastTickLine.endZ) - startZ) * renderProgress);
        GlStateManager.rotated(rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotated(rotationPitch, 0.0F, 0.0F, 1.0F);
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        RenderUtils.glColorHex(0xFF000000 | color);
        double size = 5 / 16D;
        for (int i = 0; i < PneumaticCraftUtils.CIRCLE_POINTS; i++) {
            wr.pos(0, PneumaticCraftUtils.sin[i] * size, PneumaticCraftUtils.cos[i] * size).endVertex();
        }
        Tessellator.getInstance().draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.popMatrix();
    }
}
