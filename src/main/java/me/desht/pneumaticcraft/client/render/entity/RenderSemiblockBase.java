package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySemiblockBase;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.LightType;

import static net.minecraft.util.Direction.*;

abstract class RenderSemiblockBase<T extends EntitySemiblockBase> extends EntityRenderer<T> {
    // not the usual enum order: down last because it's the least likely candidate
    private static final Direction[] LIGHTING_DIRS = new Direction[] {
            UP, NORTH, SOUTH, WEST, EAST, DOWN
    };

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
            Vector3d look = Minecraft.getInstance().player.getLook(partialTicks);
            Vector3f wobble = new Vector3f((float)look.getZ(), 0.0F, -(float)look.getX());
            matrixStack.rotate(wobble.rotationDegrees(MathHelper.sin(f) * f * f1 / 10.0F * 1));
        }
    }

    /**
     * This is a cheat since semiblocks rendering on solid blocks will otherwise appear unlit; use the lighting
     * level of an adjacent non-solid block instead.
     *
     * @param entityIn the entity
     * @param packedLight the initial packed light level
     * @return a possibly modified packed light level
     */
    int kludgeLightingLevel(T entityIn, int packedLight) {
        if (packedLight == 0) {
            BlockPos pos = entityIn.getBlockPos();
            for (Direction d : LIGHTING_DIRS) {
                BlockPos pos2 = pos.offset(d);
                if (!Block.hasEnoughSolidSide(entityIn.world, pos2, d.getOpposite())) {
                    int block = entityIn.world.getLightFor(LightType.BLOCK, pos2);
                    int sky = entityIn.world.getLightFor(LightType.SKY, pos2);
                    return LightTexture.packLight(block, sky);
                }
            }
        }
        return packedLight;
    }
}
