package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.SafetyValveModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderSafetyValveModule extends TubeModuleRendererBase<SafetyValveModule> {
    private final ModelPart tubeConnector;
    private final ModelPart valve;
    private final ModelPart valveHandle;
    private final ModelPart valveLid;

    private static final String TUBECONNECTOR = "tubeConnector";
    private static final String VALVE = "valve";
    private static final String VALVEHANDLE = "valveHandle";
    private static final String VALVELID = "valveLid";

    public RenderSafetyValveModule(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.SAFETY_VALVE_MODULE);
        tubeConnector = root.getChild(TUBECONNECTOR);
        valve = root.getChild(VALVE);
        valveHandle = root.getChild(VALVEHANDLE);
        valveLid = root.getChild(VALVELID);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(TUBECONNECTOR, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("tubeConnector_0", -0.5F, -0.5F, 0.0F, 4, 4, 2),
                PartPose.offset(-1.5F, 14.5F, 2.0F));
        partdefinition.addOrReplaceChild(VALVE, CubeListBuilder.create().texOffs(0, 6)
                        .addBox("valve_0", 0.0F, 0.0F, 0.0F, 2, 2, 4),
                PartPose.offset(-1.0F, 15.0F, 4.0F));
        partdefinition.addOrReplaceChild(VALVEHANDLE, CubeListBuilder.create().texOffs(0, 16)
                        .addBox("valveHandle_0", 0.5592F, 0.0F, 0.829F, 1, 1, 3),
                PartPose.offsetAndRotation(2.0F, 15.5F, 4.0F, 0.0F, -0.5934F, 0.0F));
        partdefinition.addOrReplaceChild(VALVELID, CubeListBuilder.create().texOffs(0, 12)
                        .addBox("valveLid_0", -3.0F, -1.0F, 0.0F, 3, 3, 1, 0, 12),
                PartPose.offset(1.5F, 15.5F, 7.25F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }


    @Override
    protected void renderDynamic(SafetyValveModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, float alpha) {
        tubeConnector.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        valve.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        valveHandle.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
        valveLid.render(matrixStack, builder, combinedLight, combinedOverlay, 1f, 1f, 1f, alpha);
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_SAFETY_VALVE_UPGRADED;
        } else {
            texture = Textures.MODEL_SAFETY_VALVE;
        }
        return texture;
    }
}
