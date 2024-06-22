package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.tubemodules.LogisticsModule;
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
import net.minecraft.world.item.DyeColor;

import static net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

public class LogisticsRenderer extends AbstractTubeModuleRenderer<LogisticsModule> {
    private final ModelPart base2;
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;
    private final ModelPart shape4;
    private final ModelPart notPowered;
    private final ModelPart powered;
    private final ModelPart action;
    private final ModelPart notEnoughAir;

    private static final String BASE2 = "base2";
    private static final String SHAPE1 = "shape1";
    private static final String SHAPE2 = "shape2";
    private static final String SHAPE3 = "shape3";
    private static final String SHAPE4 = "shape4";
    private static final String NOTPOWERED = "notPowered";
    private static final String POWERED = "powered";
    private static final String ACTION = "action";
    private static final String NOTENOUGHAIR = "notEnoughAir";

    public LogisticsRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(PNCModelLayers.LOGISTICS_MODULE);
        base2 = root.getChild(BASE2);
        shape1 = root.getChild(SHAPE1);
        shape2 = root.getChild(SHAPE2);
        shape3 = root.getChild(SHAPE3);
        shape4 = root.getChild(SHAPE4);
        notPowered = root.getChild(NOTPOWERED);
        powered = root.getChild(POWERED);
        action = root.getChild(ACTION);
        notEnoughAir = root.getChild(NOTENOUGHAIR);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(BASE2, CubeListBuilder.create().texOffs(0, 25)
                        .addBox("base2_0", 0F, 0F, 0F, 12F, 2F, 12F),
                PartPose.offsetAndRotation(-6F, 10F, 6F, -1.570796F, 0F, 0F));
        partdefinition.addOrReplaceChild(SHAPE1, CubeListBuilder.create().texOffs(0, 39)
                        .addBox("shape1_0", 0F, 0F, 0F, 1F, 13F, 1F),
                PartPose.offset(5.5F, 9.5F, 5.5F));
        partdefinition.addOrReplaceChild(SHAPE2, CubeListBuilder.create().texOffs(4, 39)
                        .addBox("shape2_0", 0F, 0F, 0F, 1F, 13F, 1F),
                PartPose.offset(-6.5F, 9.5F, 5.5F));
        partdefinition.addOrReplaceChild(SHAPE3, CubeListBuilder.create().texOffs(8, 39)
                        .addBox("shape3_0", 0F, 0F, 0F, 11F, 1F, 1F),
                PartPose.offset(-5.5F, 9.5F, 5.5F));
        partdefinition.addOrReplaceChild(SHAPE4, CubeListBuilder.create().texOffs(8, 41)
                        .addBox("shape4_0", 0F, 0F, 0F, 11F, 1F, 1F),
                PartPose.offset(-5.5F, 21.5F, 5.5F));
        partdefinition.addOrReplaceChild(NOTPOWERED, CubeListBuilder.create().texOffs(24, 8)
                        .addBox("notPowered_0", 0F, 0F, 0F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(-3F, 13F, 4F, -1.570796F, 0F, 0F));
        partdefinition.addOrReplaceChild(POWERED, CubeListBuilder.create().texOffs(0, 8)
                        .addBox("powered_0", 0F, 0F, 0F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(-3F, 13F, 4F, -1.570796F, 0F, 0F));
        partdefinition.addOrReplaceChild(ACTION, CubeListBuilder.create().texOffs(24, 0)
                        .addBox("action_0", 0F, 0F, 0F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(-3F, 13F, 4F, -1.570796F, 0F, 0F));
        partdefinition.addOrReplaceChild(NOTENOUGHAIR, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("notEnoughAir_0", 0F, 0F, 0F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(-3F, 13F, 4F, -1.570796F, 0F, 0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    protected void render(LogisticsModule module, PoseStack matrixStack, VertexConsumer builder, float partialTicks, int combinedLight, int combinedOverlay, int alpha) {
        ModelPart base;
        if (module.getTicksSinceAction() >= 0) {
            base = action;
        } else if (module.getTicksSinceNotEnoughAir() >= 0) {
            base = notEnoughAir;
        } else {
            base = module.hasPower() ? powered : notPowered;
        }

        int baseColor = FastColor.ARGB32.color(alpha, 0xFFFFFF);
        base.render(matrixStack, builder, FULL_BRIGHT, combinedOverlay, baseColor);
        base2.render(matrixStack, builder, combinedLight, combinedOverlay, baseColor);

        // the coloured frame
        int frameColor = FastColor.ARGB32.color(alpha, DyeColor.byId(module.getColorChannel()).getTextureDiffuseColor());
        shape1.render(matrixStack, builder, combinedLight, combinedOverlay, frameColor);
        shape2.render(matrixStack, builder, combinedLight, combinedOverlay, frameColor);
        shape3.render(matrixStack, builder, combinedLight, combinedOverlay, frameColor);
        shape4.render(matrixStack, builder, combinedLight, combinedOverlay, frameColor);
    }

    @Override
    protected ResourceLocation getTexture(boolean isUpgraded) {
        return Textures.MODEL_LOGISTICS_MODULE;
    }
}
