package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

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
        LivingEntity target = minigun.getAttackTarget();

        matrixStack.pushPose();
        matrixStack.translate(-x, -y, -z);
        Vector3d vec = new Vector3d(target.getX() - x, target.getY() + target.getBbHeight() / 2 - y, target.getZ() - z)
                .normalize().scale(gunRadius);
        // TODO don't really need ProgressingLine here
        ProgressingLine minigunFire = new ProgressingLine().setProgress(1);
        minigunFire.startX = (float) (x + vec.x);
        minigunFire.startY = (float) (y + vec.y);
        minigunFire.startZ = (float) (z + vec.z);
        Random rand = target.getCommandSenderWorld().random;
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.getBlockHilightLine(false, false));
        for (int i = 0; i < 5; i++) {
            minigunFire.endX = (float) (target.getX() + rand.nextDouble() * 0.8 - 0.4);
            minigunFire.endY = (float) (target.getY() + target.getBbHeight() / 2 + rand.nextDouble() * 0.8 - 0.4);
            minigunFire.endZ = (float) (target.getZ() + rand.nextDouble() * 0.8 - 0.4);
            RenderUtils.renderProgressingLine(minigunFire, matrixStack, builder, 0x40000000 | minigun.getAmmoColor());
        }
        matrixStack.popPose();
    }
}
