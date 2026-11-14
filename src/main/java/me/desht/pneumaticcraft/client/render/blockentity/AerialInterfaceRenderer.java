package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.common.block.entity.utility.AerialInterfaceBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

public class AerialInterfaceRenderer implements BlockEntityRenderer<AerialInterfaceBlockEntity, AerialInterfaceRenderer.State> {
    private final SkullModel headModel;
    private final PlayerSkinRenderCache playerSkinRenderCache;

    private static final double EXTRUSION = 0.05;  // how far the head sticks out of the main block

    public AerialInterfaceRenderer(BlockEntityRendererProvider.Context ctx) {
        headModel = new SkullModel(ctx.bakeLayer(ModelLayers.PLAYER_HEAD));
        playerSkinRenderCache = ctx.playerSkinRenderCache();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AerialInterfaceBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.rotation = blockEntity.getRotation();
        state.headRenderType = blockEntity.gameProfileClient == null ? null :
                playerSkinRenderCache.getOrDefault(blockEntity.gameProfileClient).renderType();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // code adapted from SkullBlockRenderer
        if (state.headRenderType != null) {
            Direction dir = state.rotation;

            poseStack.pushPose();
            poseStack.translate(0.5 + dir.getStepX() * 0.25, 0.5D + (0.25 + EXTRUSION), 0.5 + dir.getStepZ() * 0.25);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            SkullModelBase.State headState = new SkullModelBase.State();
            headState.yRot = dir.getOpposite().get2DDataValue() * 90F;
            headState.xRot = -90F;
            submitNodeCollector.submitModel(headModel, headState, poseStack, state.headRenderType, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
            poseStack.popPose();
        }
    }

    public static class State extends BlockEntityRenderState {
        Direction rotation = Direction.NORTH;
        @Nullable RenderType headRenderType;
    }
}
