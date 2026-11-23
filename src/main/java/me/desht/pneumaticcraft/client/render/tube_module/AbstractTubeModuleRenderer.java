package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Renders one type of tube module. Called from the pressure tube's block entity renderer.
 * @param <T> the module type
 * @param <S> the render state type
 */
public abstract class AbstractTubeModuleRenderer<T extends AbstractTubeModule, S extends AbstractTubeModuleRenderer.ModuleRenderState> {
    public abstract S createRenderState();

    public void extractRenderState(T module, S state, float partialTicks, Vec3 cameraPosition) {
        state.direction = module.getDirection();
        state.fake = module.isFake();
        state.upgraded = module.isUpgraded();
    }

    public final void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        poseStack.pushPose();

        // transforms to get model orientation right
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(1f, -1f, -1f);

        RenderUtils.rotateMatrixForDirection(poseStack, state.direction);

        Identifier texture = getTexture(state.upgraded);
        RenderType renderType = state.fake ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutoutCull(texture);
        submitModel(state, poseStack, new ModuleSubmitter(submitNodeCollector, renderType, lightCoords, withModuleAlpha(state.fake, 0xFFFFFF), breakProgress));

        poseStack.popPose();

        submitExtras(state, poseStack, submitNodeCollector);
    }

    protected abstract void submitModel(S state, PoseStack poseStack, ModuleSubmitter submitter);

    protected abstract Identifier getTexture(boolean isUpgraded);

    protected void submitExtras(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        // nothing; override in subclasses
    }

    /**
     * Fake (preview) modules are drawn semi-transparent.
     */
    protected static int withModuleAlpha(boolean fake, int rgb) {
        return ARGB.color(fake ? 80 : 255, rgb);
    }

    public static class ModuleRenderState {
        public Direction direction = Direction.NORTH;
        public boolean fake;
        public boolean upgraded;
    }

    /**
     * Submits model parts for a module, with the render type, light and alpha already worked out.
     */
    public record ModuleSubmitter(SubmitNodeCollector collector, RenderType renderType, int lightCoords, int baseColor, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        public void submitPart(ModelPart part, PoseStack poseStack) {
            submitPart(part, poseStack, lightCoords, baseColor);
        }

        public void submitPart(ModelPart part, PoseStack poseStack, int color) {
            submitPart(part, poseStack, lightCoords, color);
        }

        public void submitPart(ModelPart part, PoseStack poseStack, int light, int color) {
            collector.submitModelPart(part, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, null, color, breakProgress);
        }
    }
}
