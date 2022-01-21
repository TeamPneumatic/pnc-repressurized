package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelLogisticsFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderLogisticsFrame extends RenderSemiblockBase<EntityLogisticsFrame> {
    private final ModelLogisticsFrame model;

    public RenderLogisticsFrame(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelLogisticsFrame(ctx.bakeLayer(PNCModelLayers.LOGISTICS_FRAME));
    }

    @Override
    public void render(EntityLogisticsFrame entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
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
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90));
                matrixStackIn.translate(0, -1, 0);
                break;
            case DOWN:
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-90));
                matrixStackIn.translate(0, -1, 0);
                break;
            case NORTH:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
                break;
            case SOUTH:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
                break;
            case WEST:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180));
                break;
            case EAST:
                break;
        }

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        model.renderToBuffer(matrixStackIn, builder, kludgeLightingLevel(entity, packedLightIn), OverlayTexture.pack(0F, false), 1f, 1f, 1f, alpha);

        matrixStackIn.popPose();
    }

    @Override
    public Vec3 getRenderOffset(EntityLogisticsFrame entityIn, float partialTicks) {
        VoxelShape shape = entityIn.getBlockState().getShape(entityIn.getWorld(), entityIn.getBlockPos());
        double yOff = (shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y)) / 2.0;
        switch (entityIn.getSide()) {
            case DOWN: return new Vec3(0, shape.min(Direction.Axis.Y), 0);
            case UP: return new Vec3(0, shape.max(Direction.Axis.Y) - 1, 0);
            case NORTH: return new Vec3(0, yOff - 0.5, shape.min(Direction.Axis.Z));
            case SOUTH: return new Vec3(0, yOff - 0.5, shape.max(Direction.Axis.Z) - 1);
            case WEST: return new Vec3(shape.min(Direction.Axis.X), yOff - 0.5, 0);
            case EAST: return new Vec3(shape.max(Direction.Axis.X) - 1, yOff - 0.5, 0);
            default: return Vec3.ZERO;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLogisticsFrame entityLogisticsFrame) {
        return entityLogisticsFrame.getTexture();
    }
}
