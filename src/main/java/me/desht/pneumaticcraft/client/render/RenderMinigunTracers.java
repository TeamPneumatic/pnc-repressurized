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

package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static me.desht.pneumaticcraft.common.minigun.Minigun.MAX_GUN_SPEED;

public class RenderMinigunTracers {
    public static boolean shouldRender(Minigun minigun) {
        return minigun.isMinigunActivated()
                && minigun.getMinigunSpeed() == MAX_GUN_SPEED
                && minigun.isGunAimedAtTarget()
                && minigun.getAttackTarget() != null;
    }

    public static void render(Minigun minigun, PoseStack matrixStack, MultiBufferSource buffer, double x, double y, double z, double gunRadius) {
        LivingEntity target = minigun.getAttackTarget();

        matrixStack.pushPose();
        matrixStack.translate(-x, -y, -z);
        Vec3 vec = new Vec3(target.getX() - x, target.getY() + target.getBbHeight() / 2 - y, target.getZ() - z)
                .normalize().scale(gunRadius);
        // TODO don't really need ProgressingLine here
        ProgressingLine minigunFire = new ProgressingLine().setProgress(1);
        minigunFire.startX = (float) (x + vec.x);
        minigunFire.startY = (float) (y + vec.y);
        minigunFire.startZ = (float) (z + vec.z);
        Random rand = target.getCommandSenderWorld().random;
        VertexConsumer builder = buffer.getBuffer(ModRenderTypes.getBlockHilightLine(false, false));
        for (int i = 0; i < 5; i++) {
            minigunFire.endX = (float) (target.getX() + rand.nextDouble() * 0.8 - 0.4);
            minigunFire.endY = (float) (target.getY() + target.getBbHeight() / 2 + rand.nextDouble() * 0.8 - 0.4);
            minigunFire.endZ = (float) (target.getZ() + rand.nextDouble() * 0.8 - 0.4);
            RenderUtils.renderProgressingLine(minigunFire, matrixStack, builder, 0x40000000 | minigun.getAmmoColor());
        }
        matrixStack.popPose();
    }
}
