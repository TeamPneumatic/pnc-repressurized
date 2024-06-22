package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelLogisticsFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderLogisticsFrame extends RenderSemiblockBase<AbstractLogisticsFrameEntity> {
    private final ModelLogisticsFrame model;

    public RenderLogisticsFrame(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelLogisticsFrame(ctx.bakeLayer(PNCModelLayers.LOGISTICS_FRAME));
    }

    @Override
    public void render(AbstractLogisticsFrameEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        float alpha = entity.getAlpha() / 255F;
        if (alpha == 0f) return;

        if (entity.isAir()) {
            return;
        }

        matrixStackIn.pushPose();

        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }

        Direction side = entity.getSide();
        matrixStackIn.translate(0, side.getAxis() == Direction.Axis.Y ? 0.5 : -0.5, 0);
        switch (side) {
            case UP:
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(90));
                matrixStackIn.translate(0, -1, 0);
                break;
            case DOWN:
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-90));
                matrixStackIn.translate(0, -1, 0);
                break;
            case NORTH:
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case SOUTH:
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(-90));
                break;
            case WEST:
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case EAST:
                break;
        }

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        int color = FastColor.ARGB32.color((int) (255 * alpha), 255, 255, 255);
        model.renderToBuffer(matrixStackIn, builder, kludgeLightingLevel(entity, packedLightIn), OverlayTexture.pack(0F, false), color);

        matrixStackIn.popPose();
    }

    @Override
    public Vec3 getRenderOffset(AbstractLogisticsFrameEntity entityIn, float partialTicks) {
        VoxelShape shape = entityIn.getBlockState().getShape(entityIn.getWorld(), entityIn.getBlockPos());
        double yOff = (shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y)) / 2.0;
        return switch (entityIn.getSide()) {
            case DOWN -> new Vec3(0, shape.min(Direction.Axis.Y), 0);
            case UP -> new Vec3(0, shape.max(Direction.Axis.Y) - 1, 0);
            case NORTH -> new Vec3(0, yOff - 0.5, shape.min(Direction.Axis.Z));
            case SOUTH -> new Vec3(0, yOff - 0.5, shape.max(Direction.Axis.Z) - 1);
            case WEST -> new Vec3(shape.min(Direction.Axis.X), yOff - 0.5, 0);
            case EAST -> new Vec3(shape.max(Direction.Axis.X) - 1, yOff - 0.5, 0);
        };
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractLogisticsFrameEntity entityLogisticsFrame) {
        return entityLogisticsFrame.getTexture();
    }
}
