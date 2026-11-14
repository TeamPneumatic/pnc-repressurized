package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.processing.AssemblyControllerBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class AssemblyControllerRenderer extends AbstractBlockEntityModelRenderer<AssemblyControllerBlockEntity, AssemblyControllerRenderer.State> {
    private static final float TEXT_SIZE = 0.007F;
    private final ModelPart screen;

    private static final String SCREEN = "screen";

    public AssemblyControllerRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.ASSEMBLY_CONTROLLER);
        screen = root.getChild(SCREEN);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(SCREEN, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("screen_0", -1.0F, 0.0F, -1.0F, 12, 6, 2, 16, 0)
                        .mirror(),
                PartPose.offsetAndRotation(-5.0F, 8.0F, 1.0F, -0.5934119F, 0, 0));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AssemblyControllerBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.text = Component.literal("> " + blockEntity.displayedText).getVisualOrderText();
        state.hasProblem = blockEntity.hasProblem;
    }

    @Override
    public void submitModel(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // have the screen face the player
        poseStack.mulPose(Axis.YP.rotationDegrees(180 + camera.yRot));

        submitPart(submitNodeCollector, screen, poseStack, RenderTypes.entityCutout(Textures.MODEL_ASSEMBLY_CONTROLLER), state);

        // status text
        poseStack.translate(-0.23D, 0.50D, -0.04D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-34));
        poseStack.scale(TEXT_SIZE, TEXT_SIZE, TEXT_SIZE);
        submitNodeCollector.submitText(poseStack, 1, 4, state.text, false, Font.DisplayMode.NORMAL, state.lightCoords, 0xFF4ce568, 0, 0);

        // possible problem icon
        if (state.hasProblem) {
            submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.getTextureRenderColored(Textures.GUI_GREEN_PROBLEMS_TEXTURE), (pose, builder) ->
                    RenderUtils.drawTexture(pose.pose(), builder, 0, 18, state.lightCoords)
            );
        }
    }

    public static class State extends ModelRenderState {
        FormattedCharSequence text = FormattedCharSequence.EMPTY;
        boolean hasProblem;
    }
}
