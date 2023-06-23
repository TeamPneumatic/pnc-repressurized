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
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.ElevatorCallerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

public class ElevatorCallerRenderer implements BlockEntityRenderer<ElevatorCallerBlockEntity> {

    private static final float Z_OFFSET = 0.499F;

    private final Font font;

    public ElevatorCallerRenderer(BlockEntityRendererProvider.Context ctx) {
        font = ctx.getFont();
    }

    @Override
    public void render(ElevatorCallerBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 1.5, 0.5);
        matrixStackIn.scale(1f, -1f, -1f);
        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        matrixStackIn.translate(-1, 0, -1);

        Matrix4f posMat = matrixStackIn.last().pose();

        for (ElevatorCallerBlockEntity.ElevatorButton button : te.getFloors()) {
            // button background
            VertexConsumer builder = bufferIn.getBuffer(ModRenderTypes.UNTEXTURED_QUAD);
            builder.vertex(posMat, button.posX + 0.5F, button.posY + 0.5F, Z_OFFSET)
                    .color(button.red, button.green, button.blue, 0.1F)
                    .uv2(0x00F000A0)
                    .endVertex();
            builder.vertex(posMat, button.posX + 0.5F, button.posY + button.height + 0.5F, Z_OFFSET)
                    .color(button.red, button.green, button.blue, 0.1F)
                    .uv2(0x00F000A0)
                    .endVertex();
            builder.vertex(posMat, button.posX + button.width + 0.5F, button.posY + button.height + 0.5F, Z_OFFSET)
                    .color(button.red, button.green, button.blue, 0.1F)
                    .uv2(0x00F000A0)
                    .endVertex();
            builder.vertex(posMat, button.posX + button.width + 0.5F, button.posY + 0.5F, Z_OFFSET)
                    .color(button.red, button.green, button.blue, 0.1F)
                    .uv2(0x00F000A0)
                    .endVertex();

            // button text
            matrixStackIn.pushPose();
            matrixStackIn.translate(button.posX + 0.5D, button.posY + 0.5D, 0.498);
            matrixStackIn.translate(button.width / 2, button.height / 2, 0);
            float textScale = Math.min(button.width / 10F, button.height / 10F);
            matrixStackIn.scale(textScale, textScale, textScale);
            font.drawInBatch(button.buttonText, -font.width(button.buttonText) / 2f, -font.lineHeight / 2f, 0xFFFFFFFF, false, matrixStackIn.last().pose(), bufferIn, Font.DisplayMode.NORMAL, 50, combinedLightIn);
            matrixStackIn.popPose();
        }

        matrixStackIn.popPose();
    }
}
