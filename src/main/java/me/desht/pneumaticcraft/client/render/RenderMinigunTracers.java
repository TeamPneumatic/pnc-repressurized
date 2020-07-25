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

        matrixStack.push();
        matrixStack.translate(-x, -y, -z);
        Vector3d vec = new Vector3d(target.getPosX() - x, target.getPosY() + target.getHeight() / 2 - y, target.getPosZ() - z)
                .normalize().scale(gunRadius);
        // TODO don't really need ProgressingLine here
        ProgressingLine minigunFire = new ProgressingLine().setProgress(1);
        minigunFire.startX = (float) (x + vec.x);
        minigunFire.startY = (float) (y + vec.y);
        minigunFire.startZ = (float) (z + vec.z);
        Random rand = target.getEntityWorld().rand;
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.getBlockHilightLine(false));
        for (int i = 0; i < 5; i++) {
            minigunFire.endX = (float) (target.getPosX() + rand.nextDouble() * 0.8 - 0.4);
            minigunFire.endY = (float) (target.getPosY() + target.getHeight() / 2 + rand.nextDouble() * 0.8 - 0.4);
            minigunFire.endZ = (float) (target.getPosZ() + rand.nextDouble() * 0.8 - 0.4);
            RenderUtils.renderProgressingLine(minigunFire, matrixStack, builder, 0x40000000 | minigun.getAmmoColor());
        }
        matrixStack.pop();
    }
}
