package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.HeatFrameEntity;
import me.desht.pneumaticcraft.common.heat.TemperatureCategory;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.AABB;

public class RenderHeatFrame extends RenderSemiblockBase<HeatFrameEntity, RenderHeatFrame.State> {
    private final ModelHeatFrame model;

    public RenderHeatFrame(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelHeatFrame(ctx.bakeLayer(PNCModelLayers.HEAT_FRAME));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HeatFrameEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        AABB aabb = entity.getBoundingBox();
        state.xSize = (float) aabb.getXsize();
        state.ySize = (float) aabb.getYsize();
        state.zSize = (float) aabb.getZsize();
        state.texture = TemperatureCategory.forTemperature(entity.getSyncedTemperature()).getTextureLocation();
        state.lightCoords = kludgeLightingLevel(entity, state.lightCoords);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(state.xSize, state.ySize, state.zSize);
        poseStack.translate(0, 1.5, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(180F));
        wobble(state, poseStack);

        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, model.renderType(state.texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public static class State extends SemiblockRenderState {
        float xSize;
        float ySize;
        float zSize;
        Identifier texture;
    }
}
