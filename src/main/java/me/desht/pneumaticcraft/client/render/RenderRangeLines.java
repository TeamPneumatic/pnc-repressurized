package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RenderRangeLines {
    private final List<RenderProgressingLine> rangeLines = new ArrayList<>();
    private int rangeLinesTimer = 0;
    private final int color;
    private final BlockPos pos;

    public RenderRangeLines(int color) {
        this(color, null);
    }

    public RenderRangeLines(int color, BlockPos pos) {
        this.color = color;
        this.pos = pos;
    }

    public void resetRendering(double range) {
        rangeLinesTimer = 120;

        rangeLines.clear();
        double renderRange = range + 0.5D;
        for (int i = 0; i < range * 16 + 8; i++) {
            //Add the vertical lines of the walls
            rangeLines.add(new RenderProgressingLine(-renderRange + i / 8D, -renderRange + 1, -renderRange, -renderRange + i / 8D, renderRange + 1, -renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, -renderRange + 1, renderRange, renderRange - i / 8D, renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + 1, renderRange - i / 8D, -renderRange, renderRange + 1, renderRange - i / 8D));
            rangeLines.add(new RenderProgressingLine(renderRange, -renderRange + 1, -renderRange + i / 8D, renderRange, renderRange + 1, -renderRange + i / 8D));

            //Add the horizontal lines of the walls
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + i / 8D + 1, -renderRange, -renderRange, -renderRange + i / 8D + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange, -renderRange + i / 8D + 1, -renderRange, renderRange, -renderRange + i / 8D + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, renderRange - i / 8D + 1, -renderRange, renderRange, renderRange - i / 8D + 1, -renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + i / 8D + 1, renderRange, renderRange, -renderRange + i / 8D + 1, renderRange));

            //Add the roof and floor
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, -renderRange + 1, -renderRange, renderRange - i / 8D, -renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(renderRange - i / 8D, renderRange + 1, -renderRange, renderRange - i / 8D, renderRange + 1, renderRange));
            rangeLines.add(new RenderProgressingLine(-renderRange, -renderRange + 1, -renderRange + i / 8D, renderRange, -renderRange + 1, -renderRange + i / 8D));
            rangeLines.add(new RenderProgressingLine(-renderRange, renderRange + 1, -renderRange + i / 8D, renderRange, renderRange + 1, -renderRange + i / 8D));

        }
    }

    public boolean isCurrentlyRendering() {
        return rangeLines.size() > 0;
    }

    public void update() {
        if (rangeLinesTimer > 0) {
            rangeLinesTimer--;
            for (RenderProgressingLine line : rangeLines) {
                if (line.getProgress() > 0.005F || Minecraft.getInstance().world.rand.nextInt(15) == 0) {
                    line.incProgress(0.025F);
                }
            }
        } else {
            Iterator<RenderProgressingLine> iterator = rangeLines.iterator();
            while (iterator.hasNext()) {
                RenderProgressingLine line = iterator.next();
                if (line.getProgress() > 0.005F) {
                    line.incProgress(0.025F);
                }
                if (Minecraft.getInstance().world.rand.nextInt(8) == 0) {
                    iterator.remove();
                }
            }
        }
    }

    public void render() {
        if (rangeLines.isEmpty()) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtils.glColorHex(color);
        PlayerEntity player = ClientUtils.getClientPlayer();
        if (pos != null) {
            GlStateManager.translated(pos.getX() - player.posX + 0.5, pos.getY() - player.posY + 0.5, pos.getZ() - player.posZ + 0.5);
        }
        GlStateManager.lineWidth(2.0F);
        for (RenderProgressingLine line : rangeLines) {
            line.render();
        }
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
    }
}
