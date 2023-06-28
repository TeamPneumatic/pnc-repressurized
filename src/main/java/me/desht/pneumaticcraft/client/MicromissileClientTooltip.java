package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import org.joml.Matrix4f;

import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record MicromissileClientTooltip(MicromissilesItem.Tooltip component) implements ClientTooltipComponent {
    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, MultiBufferSource.BufferSource pBufferSource) {
        if (NBTUtils.hasTag(component.stack(), MicromissilesItem.NBT_TOP_SPEED)) {
            pY += 3;
            @SuppressWarnings("ConstantConditions") int col = ChatFormatting.GRAY.getColor();
            pFont.drawInBatch(xlate("pneumaticcraft.gui.micromissile.topSpeed"), pX, pY, col, false, pMatrix4f, pBufferSource,
                    Font.DisplayMode.NORMAL, 0, RenderUtils.FULL_BRIGHT);
            pFont.drawInBatch(xlate("pneumaticcraft.gui.micromissile.turnSpeed"), pX, pY + pFont.lineHeight + 1, col, false, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, RenderUtils.FULL_BRIGHT);
            pFont.drawInBatch(xlate("pneumaticcraft.gui.micromissile.damage"), pX, pY + pFont.lineHeight * 2 + 2, col, false, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, RenderUtils.FULL_BRIGHT);
        }
    }

    @Override
    public void renderImage(Font pFont, int pMouseX, int pMouseY, GuiGraphics graphics) {
        if (NBTUtils.hasTag(component.stack(), MicromissilesItem.NBT_TOP_SPEED)) {
            pMouseY += 3;
            int vSpace = pFont.lineHeight + 1;
            int width = pFont.width(xlate("pneumaticcraft.gui.micromissile.topSpeed"));
            width = Math.max(width, pFont.width(xlate("pneumaticcraft.gui.micromissile.turnSpeed")));
            width = Math.max(width, pFont.width(xlate("pneumaticcraft.gui.micromissile.damage")));
            int barX = width + 5;
            int barY = 0;
            int barW = getWidth(pFont) - width - 10;
            graphics.pose().pushPose();
            graphics.pose().translate(pMouseX, pMouseY, 0);
            CompoundTag tag = Objects.requireNonNull(component.stack().getTag());
            drawLine(graphics, barX, barY, barW, tag.getFloat(MicromissilesItem.NBT_TOP_SPEED));
            drawLine(graphics, barX, barY + vSpace, barW, tag.getFloat(MicromissilesItem.NBT_TURN_SPEED));
            drawLine(graphics, barX, barY + 2 * vSpace, barW, tag.getFloat(MicromissilesItem.NBT_DAMAGE));
            graphics.pose().popPose();
        }
    }

    @Override
    public int getHeight() {
        return NBTUtils.hasTag(component.stack(), MicromissilesItem.NBT_TOP_SPEED) ? 33 : 0;
    }

    @Override
    public int getWidth(Font pFont) {
        return 150;
    }

    private static void drawLine(GuiGraphics graphics, int x, int y, int totalWidth, float amount) {
        int w1 = (int)(totalWidth * amount);
        graphics.fill(x, y, x + totalWidth, y + 9, 0xFF181818);
        graphics.fill(x, y + 1, x + w1, y + 8, 0xFF00C000);
        for (int i = x + 3; i < x + w1; i += 4) {
            graphics.fill(i, y + 1, i + 1, y + 8, 0xFF006000);
        }
    }
}
