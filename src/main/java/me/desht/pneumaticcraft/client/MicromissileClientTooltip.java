package me.desht.pneumaticcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record MicromissileClientTooltip(MicromissilesItem.Tooltip component) implements ClientTooltipComponent {
    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, MultiBufferSource.BufferSource pBufferSource) {
        @SuppressWarnings("ConstantConditions") int col = ChatFormatting.GRAY.getColor();
        pFont.drawInBatch(xlate("pneumaticcraft.gui.micromissile.topSpeed"), pX, pY, col, false, pMatrix4f, pBufferSource,
                false, 0, RenderUtils.FULL_BRIGHT);
        pFont.drawInBatch(xlate("pneumaticcraft.gui.micromissile.turnSpeed"), pX, pY + pFont.lineHeight + 1, col, false, pMatrix4f, pBufferSource, false, 0, RenderUtils.FULL_BRIGHT);
        pFont.drawInBatch(xlate("pneumaticcraft.gui.micromissile.damage"), pX, pY + pFont.lineHeight * 2 + 2, col, false, pMatrix4f, pBufferSource, false, 0, RenderUtils.FULL_BRIGHT);
    }

    @Override
    public void renderImage(Font pFont, int pMouseX, int pMouseY, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset) {
        int vSpace = pFont.lineHeight + 1;
        int width = pFont.width(xlate("pneumaticcraft.gui.micromissile.topSpeed"));
        width = Math.max(width, pFont.width(xlate("pneumaticcraft.gui.micromissile.turnSpeed")));
        width = Math.max(width, pFont.width(xlate("pneumaticcraft.gui.micromissile.damage")));
        int barX = width + 5;
        int barY = 0;
        int barW = getWidth(pFont) - width - 10;
        pPoseStack.pushPose();
        pPoseStack.translate(pMouseX, pMouseY, pBlitOffset);
        drawLine(pPoseStack, barX, barY, barW, NBTUtils.getFloat(component.stack(), MicromissilesItem.NBT_TOP_SPEED));
        drawLine(pPoseStack, barX, barY + vSpace, barW, NBTUtils.getFloat(component.stack(), MicromissilesItem.NBT_TURN_SPEED));
        drawLine(pPoseStack, barX, barY + 2 * vSpace, barW, NBTUtils.getFloat(component.stack(), MicromissilesItem.NBT_DAMAGE));
        pPoseStack.popPose();
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public int getWidth(Font pFont) {
        return 150;
    }

    private static void drawLine(PoseStack matrixStack, int x, int y, int totalWidth, float amount) {
        int w1 = (int)(totalWidth * amount);
        GuiComponent.fill(matrixStack, x, y, x + totalWidth, y + 9, 0xFF181818);
        GuiComponent.fill(matrixStack, x, y + 1, x + w1, y + 8, 0xFF00C000);
        for (int i = x + 3; i < x + w1; i += 4) {
            GuiComponent.fill(matrixStack, i, y + 1, i + 1, y + 8, 0xFF006000);
        }
    }
}
