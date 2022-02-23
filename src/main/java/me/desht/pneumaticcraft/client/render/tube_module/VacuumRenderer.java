package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.VacuumModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VacuumRenderer extends AbstractTubeModuleRenderer<VacuumModule> {
    private static final int TUBE_PARTS = 4;

    private final ModelPart face;

    private static final String FACE = "face";

    public VacuumRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.VACUUM_MODULE);
        face = root.getChild(FACE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(FACE, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("face_0", -2.0F, -3.0F, -2.0F, 4, 1, 5),
                PartPose.offset(0.0F, 16.0F, 4.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    protected void render(VacuumModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, float alpha) {
        float rot = module != null ? Mth.lerp(partialTicks, module.oldRotation, module.rotation) : 0f;
        for (int i = 0; i < TUBE_PARTS; i++) {
            face.zRot = (float)i / TUBE_PARTS * 2 * (float)Math.PI + rot;
            face.render(matrixStack, builder, combinedLight, combinedOverlay);
        }
        matrixStack.scale(1.2f, 1.2f, 1.2f);
        matrixStack.translate(0, -0.17, -0.3);
        for (int i = 0; i < TUBE_PARTS; i++) {
            face.zRot = (float)i / TUBE_PARTS * 2 * (float)Math.PI + rot;
            face.render(matrixStack, builder, combinedLight, combinedOverlay);
        }
    }

    @Override
    protected ResourceLocation getTexture(boolean isUpgraded) {
        return isUpgraded ? Textures.MODEL_VACUUM_MODULE_UPGRADED : Textures.MODEL_VACUUM_MODULE;
    }
}
