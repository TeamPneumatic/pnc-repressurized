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
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.elevator.ElevatorCallerBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ElevatorCallerRenderer implements BlockEntityRenderer<ElevatorCallerBlockEntity, ElevatorCallerRenderer.State> {

    private static final float Z_OFFSET = 0.499F;
    private static final int BUTTON_LIGHT = 0x00F000A0;

    private final Font font;

    public ElevatorCallerRenderer(BlockEntityRendererProvider.Context ctx) {
        font = ctx.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ElevatorCallerBlockEntity te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);

        state.rotation = te.getRotation();
        state.buttons.clear();
        for (ElevatorCallerBlockEntity.ElevatorButton button : te.getFloors()) {
            Component text = Component.literal(button.buttonText);
            state.buttons.add(new Button(button.posX, button.posY, button.width, button.height,
                    button.red, button.green, button.blue, text.getVisualOrderText(), font.width(text)));
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(1f, -1f, -1f);
        RenderUtils.rotateMatrixForDirection(poseStack, state.rotation);
        poseStack.translate(-1, 0, -1);

        // button backgrounds
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.UNTEXTURED_QUAD, (pose, builder) -> {
            Matrix4f posMat = pose.pose();
            for (Button button : state.buttons) {
                builder.addVertex(posMat, button.posX() + 0.5F, button.posY() + 0.5F, Z_OFFSET)
                        .setColor(button.red(), button.green(), button.blue(), 0.1F)
                        .setLight(BUTTON_LIGHT);
                builder.addVertex(posMat, button.posX() + 0.5F, button.posY() + button.height() + 0.5F, Z_OFFSET)
                        .setColor(button.red(), button.green(), button.blue(), 0.1F)
                        .setLight(BUTTON_LIGHT);
                builder.addVertex(posMat, button.posX() + button.width() + 0.5F, button.posY() + button.height() + 0.5F, Z_OFFSET)
                        .setColor(button.red(), button.green(), button.blue(), 0.1F)
                        .setLight(BUTTON_LIGHT);
                builder.addVertex(posMat, button.posX() + button.width() + 0.5F, button.posY() + 0.5F, Z_OFFSET)
                        .setColor(button.red(), button.green(), button.blue(), 0.1F)
                        .setLight(BUTTON_LIGHT);
            }
        });

        // button text
        for (Button button : state.buttons) {
            poseStack.pushPose();
            poseStack.translate(button.posX() + 0.5D, button.posY() + 0.5D, 0.498);
            poseStack.translate(button.width() / 2, button.height() / 2, 0);
            float textScale = Math.min(button.width() / 10F, button.height() / 10F);
            poseStack.scale(textScale, textScale, textScale);
            submitNodeCollector.submitText(poseStack, -button.textWidth() / 2f, -font.lineHeight / 2f + 1, button.text(), false, Font.DisplayMode.NORMAL, BUTTON_LIGHT, 0xFFFFFFFF, 50, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    record Button(float posX, float posY, float width, float height, float red, float green, float blue, FormattedCharSequence text, int textWidth) {
    }

    public static class State extends BlockEntityRenderState {
        Direction rotation = Direction.NORTH;
        final List<Button> buttons = new ArrayList<>();
    }
}
