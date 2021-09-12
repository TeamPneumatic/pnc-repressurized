package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelDrone extends EntityModel<EntityDroneBase> {
    private final ModelRenderer Base;
    private final ModelRenderer Prop1Part1;
    private final ModelRenderer Prop1Part2;
    private final ModelRenderer Prop1Part3;
    private final ModelRenderer Prop2Part1;
    private final ModelRenderer Prop2Part2;
    private final ModelRenderer Prop2Part3;
    private final ModelRenderer Prop3Part1;
    private final ModelRenderer Prop3Part2;
    private final ModelRenderer Prop3Part3;
    private final ModelRenderer Prop4Part1;
    private final ModelRenderer Prop4Part2;
    private final ModelRenderer Prop4Part3;
    private final ModelRenderer Frame1;
    private final ModelRenderer Frame2;
    private final ModelRenderer LandingStand1;
    private final ModelRenderer LandingStand2;
    private final ModelRenderer LandingStand3;
    private final ModelRenderer LandingStand4;
    private final ModelRenderer LaserArm;
    private final ModelRenderer LaserSource;

    private float laserOffsetY = -4.5F / 16F;

    public ModelDrone() {
        texWidth = 64;
        texHeight = 32;

        Base = new ModelRenderer(64, 32, 0, 0);
        Base.addBox(0F, 0F, 0F, 6, 6, 6);
        Base.setPos(-3F, 14F, -3F);
        Base.mirror = true;

        Prop1Part1 = new ModelRenderer(64, 32, 0, 17);
        Prop1Part1.addBox(0.5F, 0F, -0.8F, 3, 1, 2);
        Prop1Part1.setPos(11.5F, 14F, 0F);
        Prop1Part1.mirror = true;
        setRotation(Prop1Part1, -0.3490659F, 0F, 0F);
        Prop1Part2 = new ModelRenderer(64, 32, 0, 17);
        Prop1Part2.addBox(-3.5F, 0F, -1.2F, 3, 1, 2);
        Prop1Part2.setPos(11.5F, 14F, 0F);
        Prop1Part2.mirror = true;
        setRotation(Prop1Part2, 0.3490659F, 0F, 0F);
        Prop1Part3 = new ModelRenderer(64, 32, 0, 20);
        Prop1Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop1Part3.setPos(11.5F, 14F, 0F);
        Prop1Part3.mirror = true;
        setRotation(Prop1Part3, 0F, 0F, 0F);
        Prop2Part1 = new ModelRenderer(64, 32, 0, 17);
        Prop2Part1.addBox(0.5F, 0F, -0.8F, 3, 1, 2);
        Prop2Part1.setPos(-11.5F, 14F, 0F);
        Prop2Part1.mirror = true;
        setRotation(Prop2Part1, -0.3490659F, 0F, 0F);
        Prop2Part2 = new ModelRenderer(64, 32, 0, 17);
        Prop2Part2.addBox(-3.5F, 0F, -1.2F, 3, 1, 2);
        Prop2Part2.setPos(-11.5F, 14F, 0F);
        Prop2Part2.mirror = true;
        setRotation(Prop2Part2, 0.3490659F, 0F, 0F);
        Prop2Part3 = new ModelRenderer(64, 32, 0, 20);
        Prop2Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop2Part3.setPos(-11.5F, 14F, 0F);
        Prop2Part3.mirror = true;
        setRotation(Prop2Part3, 0F, 0F, 0F);
        Prop3Part1 = new ModelRenderer(64, 32, 0, 17);
        Prop3Part1.addBox(-3.5F, 0F, -0.8F, 3, 1, 2);
        Prop3Part1.setPos(0F, 13.7F, -11.5F);
        Prop3Part1.mirror = true;
        setRotation(Prop3Part1, -0.3490659F, 0F, 0F);
        Prop3Part2 = new ModelRenderer(64, 32, 0, 17);
        Prop3Part2.addBox(0.5F, 0F, -1.1F, 3, 1, 2);
        Prop3Part2.setPos(0F, 14F, -11.5F);
        Prop3Part2.mirror = true;
        setRotation(Prop3Part2, 0.3490659F, 0F, 0F);
        Prop3Part3 = new ModelRenderer(64, 32, 0, 20);
        Prop3Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop3Part3.setPos(0F, 14F, -11.5F);
        Prop3Part3.mirror = true;
        setRotation(Prop3Part3, 0F, 0F, 0F);
        Prop4Part1 = new ModelRenderer(64, 32, 0, 17);
        Prop4Part1.addBox(-3.5F, 0F, -0.8F, 3, 1, 2);
        Prop4Part1.setPos(0F, 14F, 11.5F);
        Prop4Part1.mirror = true;
        setRotation(Prop4Part1, -0.3490659F, 0F, 0F);
        Prop4Part2 = new ModelRenderer(64, 32, 0, 17);
        Prop4Part2.addBox(0.5F, 0F, -1.1F, 3, 1, 2);
        Prop4Part2.setPos(0F, 14F, 11.5F);
        Prop4Part2.mirror = true;
        setRotation(Prop4Part2, 0.3490659F, 0F, 0F);
        Prop4Part3 = new ModelRenderer(64, 32, 0, 20);
        Prop4Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        Prop4Part3.setPos(0F, 14F, 11.5F);
        Prop4Part3.mirror = true;
        setRotation(Prop4Part3, 0F, 0F, 0F);

        Frame1 = new ModelRenderer(64, 32, 0, 26);
        Frame1.addBox(0F, 0F, 0F, 26, 2, 2);
        Frame1.setPos(-13F, 16F, -1F);
        Frame1.mirror = true;
        Frame2 = new ModelRenderer(64, 32, 0, 0);
        Frame2.addBox(0F, 0F, 0F, 2, 2, 26);
        Frame2.setPos(-1F, 16F, -13F);
        Frame2.mirror = true;

        LandingStand1 = new ModelRenderer(64, 32, 30, 0);
        LandingStand1.addBox(-1F, 0F, -0.5F, 1, 6, 1);
        LandingStand1.setPos(-8F, 18F, 0F);
        LandingStand1.mirror = true;
        LandingStand2 = new ModelRenderer(64, 32, 30, 0);
        LandingStand2.addBox(0F, 0F, -0.5F, 1, 6, 1);
        LandingStand2.setPos(8F, 18F, 0F);
        LandingStand2.mirror = true;
        LandingStand3 = new ModelRenderer(64, 32, 30, 0);
        LandingStand3.addBox(-0.5F, 0F, -1F, 1, 6, 1);
        LandingStand3.setPos(0F, 18F, -8F);
        LandingStand3.mirror = true;
        LandingStand4 = new ModelRenderer(64, 32, 30, 0);
        LandingStand4.addBox(-0.5F, 0F, 0F, 1, 6, 1);
        LandingStand4.setPos(0F, 18F, 8F);
        LandingStand4.mirror = true;

        LaserArm = new ModelRenderer(64, 32, 56, 0);
        LaserArm.addBox(0F, 0F, 0F, 1, 2, 1);
        LaserArm.setPos(-0.5F, 20F, -0.5F);
        LaserArm.mirror = true;
        LaserSource = new ModelRenderer(64, 32, 56, 3);
        LaserSource.addBox(0F, 0F, 0F, 2, 2, 2);
        LaserSource.setPos(-1F, 22F, -1F);
        LaserSource.mirror = true;
    }

    @Override
    public void setupAnim(EntityDroneBase drone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        Prop1Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop1Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop1Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop2Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop2Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop2Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop3Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop3Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop3Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop4Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop4Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Prop4Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Frame1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Frame2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        LandingStand1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        LandingStand2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        LandingStand3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        LandingStand4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        matrixStackIn.translate(0, laserOffsetY, 0);
        LaserArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        LaserSource.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        matrixStackIn.translate(0, -laserOffsetY, 0);
    }

    @Override
    public void prepareMobModel(EntityDroneBase drone, float par2, float par3, float partialTicks) {
        float propRotation = MathHelper.lerp(partialTicks, drone.oldPropRotation, drone.propRotation);
        Prop1Part1.yRot = propRotation;
        Prop1Part2.yRot = propRotation;
        Prop1Part3.yRot = propRotation;
        Prop2Part1.yRot = propRotation;
        Prop2Part2.yRot = propRotation;
        Prop2Part3.yRot = propRotation;
        Prop3Part1.yRot = -propRotation;
        Prop3Part2.yRot = -propRotation;
        Prop3Part3.yRot = -propRotation;
        Prop4Part1.yRot = -propRotation;
        Prop4Part2.yRot = -propRotation;
        Prop4Part3.yRot = -propRotation;

        float laserExtension = MathHelper.lerp(partialTicks, drone.oldLaserExtension, drone.laserExtension);
        laserOffsetY = (1F - laserExtension) * -4.5F / 16F;
    }

    @SuppressWarnings("SameParameterValue")
    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
