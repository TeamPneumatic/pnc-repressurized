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
import com.mojang.math.Matrix3f;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RenderTargetCircle {
    private static final float[] DRONE = { 1f, 1f, 0f };
    private static final float[] HANGING = { 0f, 1f, 1f };
    private static final float[] HOSTILE = { 1f, 0f, 0f };
    private static final float[] DEFAULT = { 0f, 1f, 0f };

    private static final double MAX_ROTATION = 8.0D;
    private static final float QUARTER_CIRCLE = (float)(Math.PI / 2);
    private static final float STEP = QUARTER_CIRCLE / 15f;

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
                for (float i = 0; i < QUARTER_CIRCLE; i += STEP) {
                    RenderUtils.posF(builder, posMat,Mth.cos(i) * size, Mth.sin(i) * size, 0)
                            .color(cols[0], cols[1], cols[2], alpha)
                            .uv2(RenderUtils.FULL_BRIGHT)
                            .endVertex();
                    RenderUtils.posF(builder, posMat,Mth.cos(i) * (size + 0.1F), Mth.sin(i) * (size + 0.1F), 0)
                            .color(cols[0], cols[1], cols[2], alpha)
                            .uv2(RenderUtils.FULL_BRIGHT)
                            .endVertex();
                }
            });

            if (renderAsTagged) {
                final Matrix3f normal = matrixStack.last().normal();
                RenderUtils.renderWithTypeAndFinish(matrixStack, buffer, ModRenderTypes.getLineLoops(3.0), (posMat, builder) -> {
                    for (float i = 0; i < QUARTER_CIRCLE; i += STEP) {
                        Vec3 v1 = new Vec3(Mth.cos(i) * size, Mth.sin(i) * size, 0);
                        Vec3 v2 = new Vec3(Mth.cos(i + STEP) * size, Mth.sin(i + STEP) * size, 0);
                        RenderUtils.posF(builder, posMat, v1.x(), v1.y(), 0)
                                .color(255, 0, 0, 255)
                                .normal(normal, (float) (v2.x() - v1.x()), (float) (v2.y() - v1.y()), 0f)
                                .endVertex();
                    }
                    for (float i = QUARTER_CIRCLE - STEP; i >= 0f; i -= STEP) {
                        Vec3 v1 = new Vec3(Mth.cos(i) * size, Mth.sin(i) * size, 0);
                        Vec3 v2 = new Vec3(Mth.cos(i + STEP) * size, Mth.sin(i + STEP) * size, 0);
                        RenderUtils.posF(builder, posMat, v1.x(), v1.y(), 0)
                                .color(255, 0, 0, 255)
                                .normal(normal, (float) (v2.x() - v1.x()), (float) (v2.y() - v1.y()), 0f)
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
