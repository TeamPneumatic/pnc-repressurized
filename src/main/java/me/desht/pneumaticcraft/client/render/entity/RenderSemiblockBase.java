package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySemiblockBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

abstract class RenderSemiblockBase<T extends EntitySemiblockBase> extends EntityRenderer<T> {
    RenderSemiblockBase(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    void wobble(T entityIn, float partialTicks, MatrixStack matrixStack) {
        float f = (float) entityIn.getTimeSinceHit() - partialTicks;
        float f1 = entityIn.getDamageTaken() - partialTicks;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f > 0.0F) {
            Vec3d look = Minecraft.getInstance().player.getLook(partialTicks);
            Vector3f wobble = new Vector3f((float)look.getZ(), 0.0F, -(float)look.getX());
            matrixStack.rotate(wobble.rotationDegrees(MathHelper.sin(f) * f * f1 / 10.0F * 1));
        }
    }
}
