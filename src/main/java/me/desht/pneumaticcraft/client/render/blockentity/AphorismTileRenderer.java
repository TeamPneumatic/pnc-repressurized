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

package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.gui.AphorismTileScreen;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.AphorismTileBlock;
import me.desht.pneumaticcraft.common.block.entity.utility.AphorismTileBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AphorismTileRenderer implements BlockEntityRenderer<AphorismTileBlockEntity, AphorismTileRenderer.State> {
    private static final float ICON_SCALE = 9f;
    private static final String REDSTONE_STR = Pattern.quote("{redstone}");

    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public AphorismTileRenderer(BlockEntityRendererProvider.Context ctx) {
        font = ctx.font();
        itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AphorismTileBlockEntity te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);

        state.rotation = te.getRotation();
        state.zOff = te.isInvisible() ? 0.01 : AphorismTileBlock.APHORISM_TILE_THICKNESS;
        state.textRotation = te.getTextRotation();

        int fh = font.lineHeight;

        AphorismTileScreen editor = Minecraft.getInstance().screen instanceof AphorismTileScreen g && g.blockEntity == te ? g : null;

        String[] textLines = te.getTextLines();
        int lineWidth = te.getMaxLineWidth(editor != null);
        float lineHeight = (fh * textLines.length) * (1 + (te.getMarginSize() + 1) * 0.075f);
        state.textScale = Math.min(14 / 16F / lineWidth, 14 / 16F / lineHeight);

        int editedLine = editor == null ? -1 : editor.cursorY;
        boolean showCursor = editor != null && (editor.updateCounter & 0xf) < 8;
        float mid = (textLines.length + 1) / 2f - 1.0f;

        state.lines.clear();
        for (int i = 0; i < textLines.length; i++) {
            if (!te.getIconAt(i).isEmpty() && editor == null) {
                ItemStackRenderState icon = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(icon, te.getIconAt(i), ItemDisplayContext.FIXED, te.getLevel(), null, 0);
                state.lines.add(new Line(icon, FormattedCharSequence.EMPTY, 0f, 8 * (mid - i)));
            } else {
                String textLine;
                if (editedLine == i) {
                    String cursor = showCursor ? "■" : "□";
                    int cx = Math.min(editor.cursorX, textLines[i].length());
                    textLine = textLines[i].substring(0, cx) + cursor + textLines[i].substring(cx);
                } else {
                    textLine = textLines[i];
                }
                if (editor == null && te.isRedstoneLine(i)) {
                    textLine = textLine.replaceAll(REDSTONE_STR, Integer.toString(te.pollRedstone()));
                }
                float x = -font.width(textLine) / 2f;
                float y = -(textLines.length * fh) / 2f + i * fh + 1;
                state.lines.add(new Line(null, Component.literal(textLine).getVisualOrderText(), x, y));
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();

        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(1, -1, -1);
        RenderUtils.rotateMatrixForDirection(poseStack, state.rotation);
        poseStack.translate(0, 1, 0.5 - state.zOff - 0.01);

        poseStack.scale(state.textScale, state.textScale, state.textScale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.textRotation * 90));

        for (Line line : state.lines) {
            if (line.icon() != null) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                poseStack.translate(0, line.y(), 0);
                poseStack.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE);
                line.icon().submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            } else {
                submitNodeCollector.submitText(poseStack, line.x(), line.y(), line.text(), false, Font.DisplayMode.NORMAL, state.lightCoords, 0xFF000000, 0, 0);
            }
        }

        poseStack.popPose();
    }

    /**
     * One line of the tile: either an item icon or a line of text
     */
    record Line(@Nullable ItemStackRenderState icon, FormattedCharSequence text, float x, float y) {
    }

    public static class State extends BlockEntityRenderState {
        Direction rotation = Direction.NORTH;
        double zOff;
        float textScale;
        int textRotation;
        final List<Line> lines = new ArrayList<>();
    }
}
