package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.Random;

import static me.desht.pneumaticcraft.common.minigun.Minigun.MAX_GUN_SPEED;

public class RenderMinigunTracers {
    private final Minigun minigun;
    private final RenderProgressingLine minigunFire;

    public RenderMinigunTracers(Minigun minigun) {
        this.minigun = minigun;
        this.minigunFire = new RenderProgressingLine().setProgress(1);
    }

    public void render(double x, double y, double z, double gunRadius) {
        LivingEntity attackTarget = minigun.getAttackTarget();

        if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == MAX_GUN_SPEED && minigun.isGunAimedAtTarget() && attackTarget != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scaled(1, 1, 1);
            GlStateManager.translated(-x, -y, -z);
            GlStateManager.disableTexture();
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            RenderUtils.glColorHex(0xFF000000 | minigun.getAmmoColor());
            Random rand = attackTarget.getEntityWorld().rand;
            Vec3d vec = new Vec3d(attackTarget.posX - x, attackTarget.posY - y, attackTarget.posZ - z).normalize();
            minigunFire.startX = x + vec.x * gunRadius;
            minigunFire.startY = y + vec.y * gunRadius;
            minigunFire.startZ = z + vec.z * gunRadius;
            for (int i = 0; i < 5; i++) {
                int stipple = 0xFFFF & ~(2 << rand.nextInt(16));
                GL11.glLineStipple(4, (short) stipple);
                minigunFire.endX = attackTarget.posX + rand.nextDouble() * 0.8 - 0.4;
                minigunFire.endY = attackTarget.posY + attackTarget.getHeight() / 2 + rand.nextDouble() * 0.8 - 0.4;
                minigunFire.endZ = attackTarget.posZ + rand.nextDouble() * 0.8 - 0.4;
                minigunFire.render();
            }
            GlStateManager.color4f(1, 1, 1, 1);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            GlStateManager.enableTexture();
            GlStateManager.popMatrix();
        }
    }
}
