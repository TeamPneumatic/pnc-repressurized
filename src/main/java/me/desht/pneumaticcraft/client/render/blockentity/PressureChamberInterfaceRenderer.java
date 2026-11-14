package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberInterfaceBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

import static me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberInterfaceBlockEntity.MAX_PROGRESS;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class PressureChamberInterfaceRenderer extends AbstractBlockEntityModelRenderer<PressureChamberInterfaceBlockEntity, PressureChamberInterfaceRenderer.State> {

    private final ModelPart inputLeft;
    private final ModelPart inputRight;
    private final ModelPart inputBottom;
    private final ModelPart inputTop;
    private final ModelPart outputLeft;
    private final ModelPart outputRight;
    private final ModelPart outputBottom;
    private final ModelPart outputTop;

    private static final String INPUTLEFT = "inputLeft";
    private static final String INPUTRIGHT = "inputRight";
    private static final String INPUTBOTTOM = "inputBottom";
    private static final String INPUTTOP = "inputTop";
    private static final String OUTPUTLEFT = "outputLeft";
    private static final String OUTPUTRIGHT = "outputRight";
    private static final String OUTPUTBOTTOM = "outputBottom";
    private static final String OUTPUTTOP = "outputTop";

    public PressureChamberInterfaceRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.PRESSURE_CHAMBER_INTERFACE);
        inputLeft = root.getChild(INPUTLEFT);
        inputRight = root.getChild(INPUTRIGHT);
        inputBottom = root.getChild(INPUTBOTTOM);
        inputTop = root.getChild(INPUTTOP);
        outputLeft = root.getChild(OUTPUTLEFT);
        outputRight = root.getChild(OUTPUTRIGHT);
        outputBottom = root.getChild(OUTPUTBOTTOM);
        outputTop = root.getChild(OUTPUTTOP);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(INPUTLEFT, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("inputLeft_0", -4.0F, -12.0F, -6.0F, 4, 8, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild(INPUTRIGHT, CubeListBuilder.create().texOffs(10, 0)
                        .addBox("inputRight_0", 0.0F, -12.0F, -6.0F, 4, 8, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild(INPUTBOTTOM, CubeListBuilder.create().texOffs(0, 9)
                        .addBox("inputBottom_0", -4.0F, -8.0F, -5.0F, 8, 4, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild(INPUTTOP, CubeListBuilder.create().texOffs(0, 14)
                        .addBox("inputTop_0", -4.0F, -12.0F, -5.0F, 8, 4, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild(OUTPUTLEFT, CubeListBuilder.create().texOffs(0, 19)
                        .addBox("outputLeft_0", -4.0F, -12.0F, 5.0F, 4, 8, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild(OUTPUTRIGHT, CubeListBuilder.create().texOffs(10, 19)
                        .addBox("outputRight_0", 0.0F, -12.0F, 5.0F, 4, 8, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild(OUTPUTBOTTOM, CubeListBuilder.create().texOffs(0, 9)
                        .addBox("outputBottom_0", -4.0F, -8.0F, 4.0F, 8, 4, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.addOrReplaceChild(OUTPUTTOP, CubeListBuilder.create().texOffs(0, 14)
                        .addBox("outputTop_0", -4.0F, -12.0F, 4.0F, 8, 4, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PressureChamberInterfaceBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        state.inputProgress = Mth.lerp(partialTicks, blockEntity.oldInputProgress, blockEntity.inputProgress) / MAX_PROGRESS;
        state.outputProgress = Mth.lerp(partialTicks, blockEntity.oldOutputProgress, blockEntity.outputProgress) / MAX_PROGRESS;
        itemModelResolver.updateForTopItem(state.item, blockEntity.getStackInInterface(), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
    }

    @Override
    public void submitModel(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        RenderType renderType = RenderTypes.entityCutout(Textures.MODEL_PRESSURE_CHAMBER_INTERFACE);

        RenderUtils.rotateMatrixForDirection(poseStack, state.rotation);

        float inputProgress = state.inputProgress;
        float outputProgress = state.outputProgress;
        if (inputProgress <= 1f) {
            poseStack.pushPose();
            poseStack.translate((1F - (float) Math.cos(inputProgress * Math.PI)) * 0.122F + 0.25, 0, 0);
            submitPart(submitNodeCollector, inputLeft, poseStack, renderType, state);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate((-1F + (float) Math.cos(inputProgress * Math.PI)) * 0.122F - 0.25, 0, 0);
            submitPart(submitNodeCollector, inputRight, poseStack, renderType, state);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, (1F - (float) Math.cos(inputProgress * Math.PI)) * 0.122F, 0);
            submitPart(submitNodeCollector, inputBottom, poseStack, renderType, state);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, (-1F + (float) Math.cos(inputProgress * Math.PI)) * 0.122F, 0);
            submitPart(submitNodeCollector, inputTop, poseStack, renderType, state);
            poseStack.popPose();
        }
        if (outputProgress < 1f) {
            poseStack.pushPose();
            poseStack.translate((1F - (float) Math.cos(outputProgress * Math.PI)) * 0.122F + 0.25, 0, 0);
            submitPart(submitNodeCollector, outputLeft, poseStack, renderType, state);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate((-1F + (float) Math.cos(outputProgress * Math.PI)) * 0.122F - 0.25, 0, 0);
            submitPart(submitNodeCollector, outputRight, poseStack, renderType, state);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, (1F - (float) Math.cos(outputProgress * Math.PI)) * 0.122F, 0);
            submitPart(submitNodeCollector, outputBottom, poseStack, renderType, state);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, (-1F + (float) Math.cos(outputProgress * Math.PI)) * 0.122F, 0);
            submitPart(submitNodeCollector, outputTop, poseStack, renderType, state);
            poseStack.popPose();
        }
    }

    @Override
    protected void submitExtras(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static class State extends ModelRenderState {
        float inputProgress;
        float outputProgress;
        final ItemStackRenderState item = new ItemStackRenderState();
    }
}
