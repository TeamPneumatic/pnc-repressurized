package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.AirGrateModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class AirGrateRenderer extends AbstractTubeModuleRenderer<AirGrateModule> {
    private final ModelPart top;
    private final ModelPart side1;
    private final ModelPart side2;
    private final ModelPart side3;
    private final ModelPart side4;
    private final ModelPart base1;
    private final ModelPart base2;
    private final ModelPart base3;

    private static final String TOP = "top";
    private static final String SIDE1 = "side1";
    private static final String SIDE2 = "side2";
    private static final String SIDE3 = "side3";
    private static final String SIDE4 = "side4";
    private static final String BASE1 = "base1";
    private static final String BASE2 = "base2";
    private static final String BASE3 = "base3";

    public AirGrateRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.AIR_GRATE_MODULE);
        top = root.getChild(TOP);
        side1 = root.getChild(SIDE1);
        side2 = root.getChild(SIDE2);
        side3 = root.getChild(SIDE3);
        side4 = root.getChild(SIDE4);
        base1 = root.getChild(BASE1);
        base2 = root.getChild(BASE2);
        base3 = root.getChild(BASE3);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(TOP, CubeListBuilder.create().texOffs(42, 19)
                        .addBox("top_0", 0F, 0F, 0F, 14F, 0F, 14F),
                PartPose.offsetAndRotation(-7F, 9F, 8F, -1.570796F, 0, 0));
        partdefinition.addOrReplaceChild(SIDE1, CubeListBuilder.create().texOffs(0, 18)
                        .addBox("side1_0", 0F, 0F, 0F, 16F, 1F, 1F),
                PartPose.offset(-8F, 23F, 7F));
        partdefinition.addOrReplaceChild(SIDE2, CubeListBuilder.create().texOffs(0, 21)
                        .addBox("side2_0", 0F, 0F, 0F, 16F, 1F, 1F),
                PartPose.offset(-8F, 8F, 7F));
        partdefinition.addOrReplaceChild(SIDE3, CubeListBuilder.create().texOffs(50, 0)
                        .addBox("side3_0", 0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(-8F, 23F, 7F, 1.570796F, 0, 0));
        partdefinition.addOrReplaceChild(SIDE4, CubeListBuilder.create().texOffs(82, 0)
                        .addBox("side4_0", 0F, 0F, 0F, 1F, 1F, 14F),
                PartPose.offsetAndRotation(7F, 23F, 7F, 1.570796F, 0, 0));
        partdefinition.addOrReplaceChild(BASE1, CubeListBuilder.create().texOffs(69, 0)
                        .addBox("base1_0", 0F, 0F, 0F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(-3F, 13F, 4F, -1.570796F, 0, 0));
        partdefinition.addOrReplaceChild(BASE2, CubeListBuilder.create().texOffs(0, 25)
                        .addBox("base2_0", 0F, 0F, 0F, 12F, 2F, 12F),
                PartPose.offsetAndRotation(-6F, 10F, 6F, -1.570796F, 0, 0));
        partdefinition.addOrReplaceChild(BASE3, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("base3_0", 2F, 0F, 0F, 16F, 1F, 16F),
                PartPose.offsetAndRotation(-10F, 8F, 7F, -1.570796F, 0, 0));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }


    @Override
    protected ResourceLocation getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_AIR_GRATE_UPGRADED : Textures.MODEL_AIR_GRATE;
    }

    @Override
    protected void render(AirGrateModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, int alpha) {
        int color = FastColor.ARGB32.color(alpha, 0xFFFFFF);
        top.render(matrixStack, builder, combinedLight, combinedOverlay, color);
        side1.render(matrixStack, builder, combinedLight, combinedOverlay, color);
        side2.render(matrixStack, builder, combinedLight, combinedOverlay, color);
        side3.render(matrixStack, builder, combinedLight, combinedOverlay, color);
        side4.render(matrixStack, builder, combinedLight, combinedOverlay, color);
        base1.render(matrixStack, builder, combinedLight, combinedOverlay, color);
        base2.render(matrixStack, builder, combinedLight, combinedOverlay, color);
        base3.render(matrixStack, builder, combinedLight, combinedOverlay, color);
    }
}
