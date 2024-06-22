package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelTransferGadget;
import me.desht.pneumaticcraft.common.entity.semiblock.TransferGadgetEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderTransferGadget extends RenderSemiblockBase<TransferGadgetEntity> {
    private final ModelTransferGadget model;

    public RenderTransferGadget(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelTransferGadget(ctx.bakeLayer(PNCModelLayers.TRANSFER_GADGET));
    }

    @Override
    public void render(TransferGadgetEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entity.isAir()) {
            return;
        }
        
        matrixStackIn.pushPose();

        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }

        Direction side = entity.getSide();
        matrixStackIn.translate(0, side.getAxis() == Direction.Axis.Y ? 1.2 : -1.1, 0);
        switch (side) {
            case UP -> {
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(90));
                matrixStackIn.translate(-1.1, -1.1, 0);
            }
            case DOWN -> {
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-90));
                matrixStackIn.translate(1.3, -1.1, 0);
            }
            case NORTH -> matrixStackIn.mulPose(Axis.YP.rotationDegrees(90));
            case SOUTH -> matrixStackIn.mulPose(Axis.YP.rotationDegrees(-90));
            case WEST -> matrixStackIn.mulPose(Axis.YP.rotationDegrees(180));
        }

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        model.renderToBuffer(matrixStackIn, builder, kludgeLightingLevel(entity, packedLightIn), OverlayTexture.pack(0F, false), 0xFFFFFFFF);

        matrixStackIn.popPose();
    }

    @Override
    public Vec3 getRenderOffset(TransferGadgetEntity entityIn, float partialTicks) {
        VoxelShape shape = entityIn.getBlockState().getShape(entityIn.getWorld(), entityIn.getBlockPos());
        double yOff = (shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y)) / 2.0;
        return switch (entityIn.getSide()) {
            case DOWN -> new Vec3(0, shape.min(Direction.Axis.Y), 0);
            case UP -> new Vec3(0, shape.max(Direction.Axis.Y), 0);
            case NORTH -> new Vec3(0, yOff, shape.min(Direction.Axis.Z) - 0.6);
            case SOUTH -> new Vec3(0, yOff, shape.max(Direction.Axis.Z) - 0.4);
            case WEST -> new Vec3(shape.min(Direction.Axis.X) - 0.6, yOff, 0);
            case EAST -> new Vec3(shape.max(Direction.Axis.X) - 0.4, yOff, 0);
        };
    }

    @Override
    public ResourceLocation getTextureLocation(TransferGadgetEntity entityTransferGadget) {
        return entityTransferGadget.getIOMode().getTexture();
    }
}
