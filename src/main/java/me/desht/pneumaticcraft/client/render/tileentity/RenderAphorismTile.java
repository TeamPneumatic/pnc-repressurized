/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.AphorismTileBlock;
import me.desht.pneumaticcraft.common.block.entity.AphorismTileBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.regex.Pattern;

public class RenderAphorismTile implements BlockEntityRenderer<AphorismTileBlockEntity> {
    private static final float ICON_SCALE = 9f;

    private final Font font;

    public RenderAphorismTile(BlockEntityRendererProvider.Context ctx) {
        font = ctx.getFont();
    }

    @Override
    public void render(AphorismTileBlockEntity te, float v, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        matrixStack.pushPose();

        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1, -1, -1);
        RenderUtils.rotateMatrixForDirection(matrixStack, te.getRotation());
        double zOff = te.isInvisible() ? 0.01 : AphorismTileBlock.APHORISM_TILE_THICKNESS;
        matrixStack.translate(0, 1, 0.5 - zOff - 0.01);

        int fh = font.lineHeight;

        GuiAphorismTile editor = getEditor(te);

        String[] textLines = te.getTextLines();
        int lineWidth = te.getMaxLineWidth(editor != null);
        float lineHeight = (fh * textLines.length) * (1 + (te.getMarginSize() + 1) * 0.075f);
        float textScale = Math.min(14 / 16F / lineWidth, 14 / 16F / lineHeight);
        matrixStack.scale(textScale, textScale, textScale);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(te.textRotation * 90));

        int editedLine = editor == null ? -1 : editor.cursorY;
        boolean showCursor = editor != null && (editor.updateCounter & 0xf) < 8;
        float mid = (textLines.length + 1) / 2f - 1.0f;

        for (int i = 0; i < textLines.length; i++) {
            if (!te.getIconAt(i).isEmpty() && editor == null) {
                matrixStack.pushPose();
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
                matrixStack.translate(0, 8 * (mid - i), 0);
                matrixStack.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE);
                Minecraft.getInstance().getItemRenderer()
                        .renderStatic(te.getIconAt(i), TransformType.FIXED, combinedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer, 0);
                matrixStack.popPose();
            } else {
                String textLine;
                if (editedLine == i) {
                    String cursor = showCursor ? "\u25a0" : "\u25a1";
                    int cx = Math.min(editor.cursorX, textLines[i].length());
                    textLine = textLines[i].substring(0, cx) + cursor + textLines[i].substring(cx);
                } else {
                    textLine = textLines[i];
                }
                float x = -font.width(textLine) / 2f;
                float y = -(textLines.length * fh) / 2f + i * fh + 1;
                if (editor == null && te.isRedstoneLine(i)) {
                    textLine = textLine.replaceAll(Pattern.quote("{redstone}"), Integer.toString(te.pollRedstone()));
                    x = -font.width(textLine) / 2f;
                }
                font.drawInBatch(textLine, x, y, 0xFF000000, false, matrixStack.last().pose(), buffer, false, 0, combinedLight);
            }
        }

        matrixStack.popPose();
    }

    private GuiAphorismTile getEditor(AphorismTileBlockEntity te) {
        return Minecraft.getInstance().screen instanceof GuiAphorismTile
                && ((GuiAphorismTile) Minecraft.getInstance().screen).tile == te ?
                (GuiAphorismTile) Minecraft.getInstance().screen : null;
    }
}
