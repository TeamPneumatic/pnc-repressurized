package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.PressureChamberInterfaceBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

import static me.desht.pneumaticcraft.common.block.entity.PressureChamberInterfaceBlockEntity.MAX_PROGRESS;

public class PressureChamberInterfaceRenderer extends AbstractBlockEntityModelRenderer<PressureChamberInterfaceBlockEntity> {

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
    public void renderModel(PressureChamberInterfaceBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_PRESSURE_CHAMBER_INTERFACE));

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        float inputProgress = Mth.lerp(partialTicks, te.oldInputProgress, te.inputProgress) / MAX_PROGRESS;
        float outputProgress = Mth.lerp(partialTicks, te.oldOutputProgress, te.outputProgress) / MAX_PROGRESS;
        if (inputProgress <= 1f) {
            // REMOVED:           matrixStackIn.scale(1F - inputProgress, 1, 1);
            matrixStackIn.pushPose();
            matrixStackIn.translate((1F - (float) Math.cos(inputProgress * Math.PI)) * 0.122F + 0.25, 0, 0);
            inputLeft.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate((-1F + (float) Math.cos(inputProgress * Math.PI)) * 0.122F - 0.25, 0, 0);
            inputRight.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (1F - (float) Math.cos(inputProgress * Math.PI)) * 0.122F, 0);
            inputBottom.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (-1F + (float) Math.cos(inputProgress * Math.PI)) * 0.122F, 0);
            inputTop.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
        if (outputProgress < 1f) {
            matrixStackIn.pushPose();
            matrixStackIn.translate((1F - (float) Math.cos(outputProgress * Math.PI)) * 0.122F + 0.25, 0, 0);
            outputLeft.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate((-1F + (float) Math.cos(outputProgress * Math.PI)) * 0.122F - 0.25, 0, 0);
            outputRight.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (1F - (float) Math.cos(outputProgress * Math.PI)) * 0.122F, 0);
            outputBottom.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();

            matrixStackIn.pushPose();
            matrixStackIn.translate(0, (-1F + (float) Math.cos(outputProgress * Math.PI)) * 0.122F, 0);
            outputTop.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
            matrixStackIn.popPose();
        }
    }

    @Override
    protected void renderExtras(PressureChamberInterfaceBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getStackInInterface().isEmpty()) {
            matrixStack.pushPose();

            matrixStack.translate(0.5, 0.5, 0.5);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel bakedModel = itemRenderer.getModel(te.getStackInInterface(), te.getLevel(), null, 0);
            itemRenderer.render(te.getStackInInterface(), ItemDisplayContext.GROUND, true, matrixStack, buffer, combinedLightIn, combinedOverlayIn, bakedModel);

            matrixStack.popPose();
        }
    }
}
