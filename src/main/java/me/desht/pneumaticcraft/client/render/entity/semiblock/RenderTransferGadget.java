package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelTransferGadget;
import me.desht.pneumaticcraft.common.entity.semiblock.TransferGadgetEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderTransferGadget extends RenderSemiblockBase<TransferGadgetEntity, RenderTransferGadget.State> {
    private final ModelTransferGadget model;

    public RenderTransferGadget(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelTransferGadget(ctx.bakeLayer(PNCModelLayers.TRANSFER_GADGET));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TransferGadgetEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        state.air = entity.isAir();
        state.side = entity.getSide();
        state.texture = entity.getIOMode().getTexture();
        state.lightCoords = kludgeLightingLevel(entity, state.lightCoords);
        state.renderOffset = getRenderOffset(entity);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.air) {
            return;
        }

        poseStack.pushPose();

        wobble(state, poseStack);

        Direction side = state.side;
        poseStack.translate(0, side.getAxis() == Direction.Axis.Y ? 1.2 : -1.1, 0);
        switch (side) {
            case UP -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                poseStack.translate(-1.1, -1.1, 0);
            }
            case DOWN -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                poseStack.translate(1.3, -1.1, 0);
            }
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }

        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, model.renderType(state.texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);

        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public Vec3 getRenderOffset(State state) {
        return state.renderOffset;
    }

    private static Vec3 getRenderOffset(TransferGadgetEntity entityIn) {
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

    public static class State extends SemiblockRenderState {
        boolean air;
        Direction side = Direction.UP;
        Identifier texture;
        Vec3 renderOffset = Vec3.ZERO;
    }
}
