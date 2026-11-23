package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.entity.semiblock.CropSupportEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.AABB;

public class RenderCropSupport extends RenderSemiblockBase<CropSupportEntity, RenderCropSupport.State> {
    private final ModelCropSupport model;

    public RenderCropSupport(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelCropSupport(ctx.bakeLayer(PNCModelLayers.CROP_SUPPORT));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CropSupportEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        AABB aabb = entity.getBoundingBox();
        state.xSize = (float) (aabb.maxX - aabb.minX);
        state.ySize = (float) (aabb.maxY - aabb.minY);
        state.zSize = (float) (aabb.maxZ - aabb.minZ);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();

        poseStack.translate(0, 0.8f, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(180F));
        wobble(state, poseStack);
        poseStack.scale(state.xSize, state.ySize, state.zSize);
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, model.renderType(Textures.MODEL_CROP_SUPPORT),
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);

        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public static class State extends SemiblockRenderState {
        float xSize;
        float ySize;
        float zSize;
    }
}
