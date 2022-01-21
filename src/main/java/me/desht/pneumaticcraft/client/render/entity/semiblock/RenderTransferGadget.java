package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelTransferGadget;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderTransferGadget extends RenderSemiblockBase<EntityTransferGadget> {
    private final ModelTransferGadget model;

    public RenderTransferGadget(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelTransferGadget(ctx.bakeLayer(PNCModelLayers.TRANSFER_GADGET));
    }

    @Override
    public void render(EntityTransferGadget entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entity.isAir()) {
            return;
        }
        
        matrixStackIn.pushPose();

        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }

        Direction side = entity.getSide();
        matrixStackIn.translate(0, side.getAxis() == Axis.Y ? 1.2 : -1.1, 0);
        switch (side) {
            case UP -> {
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90));
                matrixStackIn.translate(-1.1, -1.1, 0);
            }
            case DOWN -> {
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-90));
                matrixStackIn.translate(1.3, -1.1, 0);
            }
            case NORTH -> matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
            case SOUTH -> matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
            case WEST -> matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180));
        }

        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        model.renderToBuffer(matrixStackIn, builder, kludgeLightingLevel(entity, packedLightIn), OverlayTexture.pack(0F, false), 1f, 1f, 1f, 1f);

        matrixStackIn.popPose();
    }

    @Override
    public Vec3 getRenderOffset(EntityTransferGadget entityIn, float partialTicks) {
        VoxelShape shape = entityIn.getBlockState().getShape(entityIn.getWorld(), entityIn.getBlockPos());
        double yOff = (shape.max(Axis.Y) - shape.min(Axis.Y)) / 2.0;
        return switch (entityIn.getSide()) {
            case DOWN -> new Vec3(0, shape.min(Axis.Y), 0);
            case UP -> new Vec3(0, shape.max(Axis.Y), 0);
            case NORTH -> new Vec3(0, yOff, shape.min(Axis.Z) - 0.6);
            case SOUTH -> new Vec3(0, yOff, shape.max(Axis.Z) - 0.4);
            case WEST -> new Vec3(shape.min(Axis.X) - 0.6, yOff, 0);
            case EAST -> new Vec3(shape.max(Axis.X) - 0.4, yOff, 0);
        };
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTransferGadget entityTransferGadget) {
        return entityTransferGadget.getIOMode().getTexture();
    }
}
