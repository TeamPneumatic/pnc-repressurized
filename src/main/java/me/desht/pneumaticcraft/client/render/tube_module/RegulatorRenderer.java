package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.RegulatorModule;
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

public class RegulatorRenderer extends AbstractTubeModuleRenderer<RegulatorModule> {
    private final ModelPart tubeConnector;
    private final ModelPart valve1;
    private final ModelPart valve2;

    private static final String TUBECONNECTOR = "tubeConnector";
    private static final String VALVE1 = "valve1";
    private static final String VALVE2 = "valve2";

    public RegulatorRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.REGULATOR_MODULE);
        tubeConnector = root.getChild(TUBECONNECTOR);
        valve1 = root.getChild(VALVE1);
        valve2 = root.getChild(VALVE2);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(TUBECONNECTOR, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("tubeConnector_0", 0.0F, 0.0F, 0.0F, 7, 7, 4),
                PartPose.offset(-3.5F, 12.5F, -3.0F));
        partdefinition.addOrReplaceChild(VALVE1, CubeListBuilder.create().texOffs(0, 11)
                        .addBox("valve1_0", -0.5F, -0.5F, -3.0F, 5, 5, 4),
                PartPose.offset(-2.0F, 14.0F, 4.0F));
        partdefinition.addOrReplaceChild(VALVE2, CubeListBuilder.create().texOffs(0, 21)
                        .addBox("valve2_0", -1.0F, -1.0F, 1.0F, 6, 6, 2),
                PartPose.offset(-2.0F, 14.0F, 4.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }


    @Override
    protected void render(RegulatorModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, int alpha) {
        int baseColor = FastColor.ARGB32.color(alpha, 0xFFFFFF);
        tubeConnector.render(matrixStack, builder, combinedLight, combinedOverlay, baseColor);
        valve1.render(matrixStack, builder, combinedLight, combinedOverlay, baseColor);
        valve2.render(matrixStack, builder, combinedLight, combinedOverlay, baseColor);
    }

    @Override
    protected ResourceLocation getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_REGULATOR_MODULE_UPGRADED : Textures.MODEL_REGULATOR_MODULE;
    }
}
