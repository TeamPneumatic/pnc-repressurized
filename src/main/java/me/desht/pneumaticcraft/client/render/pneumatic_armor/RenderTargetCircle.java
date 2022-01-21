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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RenderTargetCircle {
    private static final float[] DRONE = { 1f, 1f, 0f };
    private static final float[] HANGING = { 0f, 1f, 1f };
    private static final float[] HOSTILE = { 1f, 0f, 0f };
    private static final float[] DEFAULT = { 0f, 1f, 0f };

    private static final double MAX_ROTATION = 8.0D;

    private double oldRotationAngle;
    private double rotationAngle = 0;
    private double rotationSpeed = 0;
    private double rotationAcceleration = 0;
    private final Random rand;
    private boolean renderAsTagged;
    private final float[] cols = new float[4];

    RenderTargetCircle(Entity entity) {
        rand = ThreadLocalRandom.current();
        System.arraycopy(getCircleColour(entity), 0, cols, 0, 3);
        cols[3] = 0.5f; // alpha
    }

    void setRenderingAsTagged(boolean tagged) {
        renderAsTagged = tagged;
    }

    public void update() {
        oldRotationAngle = rotationAngle;
        if (rand.nextInt(15) == 0) {
            rotationAcceleration = (rand.nextDouble() - 0.5D) / 2.5D;
        }
        rotationSpeed = Mth.clamp(rotationSpeed + rotationAcceleration, -MAX_ROTATION, MAX_ROTATION);
        rotationAngle += rotationSpeed;
    }

    public void render(PoseStack matrixStack, MultiBufferSource buffer, float size, float partialTicks, float alpha) {
        double renderRotationAngle = Mth.lerp(partialTicks, oldRotationAngle, rotationAngle);

        matrixStack.pushPose();

        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) renderRotationAngle));

        for (int pass = 0; pass < 2; pass++) {
            RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.TARGET_CIRCLE, (posMat, builder) -> {
                for (int i = 0; i < PneumaticCraftUtils.CIRCLE_POINTS / 4; i++) {
                    RenderUtils.posF(builder, posMat,PneumaticCraftUtils.cos[i] * size, PneumaticCraftUtils.sin[i] * size, 0)
                            .color(cols[0], cols[1], cols[2], alpha)
                            .endVertex();
                    RenderUtils.posF(builder, posMat,PneumaticCraftUtils.cos[i] * (size + 0.1F), PneumaticCraftUtils.sin[i] * (size + 0.1F), 0)
                            .color(cols[0], cols[1], cols[2], alpha)
                            .endVertex();
                }
            });

            if (renderAsTagged) {
                RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getLineLoopsTransparent(3.0), (posMat, builder) -> {
                    for (int i = 0; i < PneumaticCraftUtils.CIRCLE_POINTS / 4; i++) {
                        RenderUtils.posF(builder, posMat, PneumaticCraftUtils.cos[i] * size, PneumaticCraftUtils.sin[i] * size, 0)
                                .color(255, 0, 0, 255)
                                .endVertex();
                    }
                    for (int i = PneumaticCraftUtils.CIRCLE_POINTS / 4 - 1; i >= 0; i--) {
                        RenderUtils.posF(builder, posMat, PneumaticCraftUtils.cos[i] * (size + 0.1F), PneumaticCraftUtils.sin[i] * (size + 0.1F), 0)
                                .color(255, 0, 0, 255)
                                .endVertex();
                    }
                });
            }

            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        }
        matrixStack.popPose();
    }

    private float[] getCircleColour(Entity entity) {
        if (entity instanceof EntityDroneBase) {
            return DRONE;
        } else if (entity instanceof Enemy) {
            return HOSTILE;
        } else if (entity instanceof HangingEntity || entity instanceof AbstractMinecart) {
            return HANGING;
        } else {
            return DEFAULT;
        }
    }
}
