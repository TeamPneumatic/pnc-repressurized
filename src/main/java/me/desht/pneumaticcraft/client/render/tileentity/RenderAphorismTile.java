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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.BlockAphorismTile;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

import java.util.regex.Pattern;

public class RenderAphorismTile extends TileEntityRenderer<TileEntityAphorismTile> {
    private static final float ICON_SCALE = 9f;

    public RenderAphorismTile(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityAphorismTile te, float v, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        matrixStack.pushPose();

        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1, -1, -1);
        RenderUtils.rotateMatrixForDirection(matrixStack, te.getRotation());
        double zOff = te.isInvisible() ? 0.01 : BlockAphorismTile.APHORISM_TILE_THICKNESS;
        matrixStack.translate(0, 1, 0.5 - zOff - 0.01);

        FontRenderer fr = Minecraft.getInstance().getEntityRenderDispatcher().getFont();
        int fh = fr.lineHeight;

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
                        .renderStatic(te.getIconAt(i), TransformType.FIXED, combinedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer);
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
                float x = -fr.width(textLine) / 2f;
                float y = -(textLines.length * fh) / 2f + i * fh + 1;
                if (editor == null && te.isRedstoneLine(i)) {
                    textLine = textLine.replaceAll(Pattern.quote("{redstone}"), Integer.toString(te.pollRedstone()));
                    x = -fr.width(textLine) / 2f;
                }
                fr.drawInBatch(textLine, x, y, 0xFF000000, false, matrixStack.last().pose(), buffer, false, 0, combinedLight);
            }
        }

        matrixStack.popPose();
    }

    private GuiAphorismTile getEditor(TileEntityAphorismTile te) {
        return Minecraft.getInstance().screen instanceof GuiAphorismTile
                && ((GuiAphorismTile) Minecraft.getInstance().screen).tile == te ?
                (GuiAphorismTile) Minecraft.getInstance().screen : null;
    }
}
