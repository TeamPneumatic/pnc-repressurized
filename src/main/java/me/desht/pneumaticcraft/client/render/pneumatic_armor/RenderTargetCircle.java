package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Random;

public class RenderTargetCircle {
    private static final float[] DRONE = { 1f, 1f, 0f };
    private static final float[] HANGING = { 0f, 1f, 1f };
    private static final float[] HOSTILE = { 1f, 0f, 0f };
    private static final float[] DEFAULT = { 0f, 1f, 0f };

    private double oldRotationAngle;
    private double rotationAngle = 0;
    private double rotationSpeed = 0;
    private double rotationAcceleration = 0;
    private final Random rand;
    private boolean renderAsTagged;
    private final float[] cols = new float[4];

    RenderTargetCircle(Entity entity) {
        rand = new Random();
        System.arraycopy(getCircleColour(entity), 0, cols, 0, 3);
        cols[3] = 0.5f; // alpha
    }

    void setRenderingAsTagged(boolean tagged) {
        renderAsTagged = tagged;
    }

    public void update() {
        oldRotationAngle = rotationAngle;
        if (rand.nextInt(15) == 0) rotationAcceleration = (rand.nextDouble() - 0.5D) / 2.5D;
        rotationSpeed += rotationAcceleration;// * 0.05D;
        double maxSpeed = 8.0D;
        if (rotationSpeed >= maxSpeed) rotationSpeed = maxSpeed;
        if (rotationSpeed <= -maxSpeed) rotationSpeed = -maxSpeed;
        rotationAngle += rotationSpeed;// * 0.05D;
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float size, float partialTicks, float alpha) {
        double renderRotationAngle = MathHelper.lerp(partialTicks, oldRotationAngle, rotationAngle);

        matrixStack.push();

        matrixStack.rotate(Vector3f.ZP.rotationDegrees((float) renderRotationAngle));

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

            matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
        }
        matrixStack.pop();
    }

    private float[] getCircleColour(Entity entity) {
        if (entity instanceof EntityDroneBase) {
            return DRONE;
        } else if (entity instanceof IMob) {
            return HOSTILE;
        } else if (entity instanceof HangingEntity) {
            return HANGING;
        } else {
            return DEFAULT;
        }
    }
}
