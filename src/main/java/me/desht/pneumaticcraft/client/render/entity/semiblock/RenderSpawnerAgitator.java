package me.desht.pneumaticcraft.client.render.entity.semiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelSpawnerAgitator;
import me.desht.pneumaticcraft.common.entity.semiblock.SpawnerAgitatorEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;

public class RenderSpawnerAgitator extends RenderSemiblockBase<SpawnerAgitatorEntity, RenderSemiblockBase.SemiblockRenderState> {
    private final ModelSpawnerAgitator model;

    public RenderSpawnerAgitator(EntityRendererProvider.Context ctx) {
        super(ctx);

        model = new ModelSpawnerAgitator(ctx.bakeLayer(PNCModelLayers.SPAWNER_AGITATOR));
    }

    @Override
    public SemiblockRenderState createRenderState() {
        return new SemiblockRenderState();
    }

    @Override
    public void submit(SemiblockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(180F));
        wobble(state, poseStack);
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, model.renderType(Textures.MODEL_SPAWNER_AGITATOR),
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
