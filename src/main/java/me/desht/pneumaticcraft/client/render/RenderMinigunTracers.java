package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

import static me.desht.pneumaticcraft.common.minigun.Minigun.MAX_GUN_SPEED;

public class RenderMinigunTracers {
    public static boolean shouldRender(Minigun minigun) {
        return minigun.isMinigunActivated()
                && minigun.getMinigunSpeed() == MAX_GUN_SPEED
                && minigun.isGunAimedAtTarget()
                && minigun.getAttackTarget() != null;
    }

    public static void render(Minigun minigun, MatrixStack matrixStack, IRenderTypeBuffer buffer, double x, double y, double z, double gunRadius) {
        LivingEntity attackTarget = minigun.getAttackTarget();

        matrixStack.push();
        matrixStack.translate(-x, -y, -z);
        Vec3d vec = new Vec3d(attackTarget.getPosX() - x, attackTarget.getPosY() - y, attackTarget.getPosZ() - z).normalize();
        ProgressingLine minigunFire = new ProgressingLine().setProgress(1);
        Random rand = attackTarget.getEntityWorld().rand;
        minigunFire.startX = x + vec.x * gunRadius;
        minigunFire.startY = y + vec.y * gunRadius;
        minigunFire.startZ = z + vec.z * gunRadius;
        IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
        for (int i = 0; i < 5; i++) {
            minigunFire.endX = attackTarget.getPosX() + rand.nextDouble() * 0.8 - 0.4;
            minigunFire.endY = attackTarget.getPosY() + attackTarget.getHeight() / 2 + rand.nextDouble() * 0.8 - 0.4;
            minigunFire.endZ = attackTarget.getPosZ() + rand.nextDouble() * 0.8 - 0.4;
            RenderUtils.renderProgressingLine(minigunFire, matrixStack, builder, 0x60000000 | minigun.getAmmoColor());
        }
        matrixStack.pop();
    }
}
