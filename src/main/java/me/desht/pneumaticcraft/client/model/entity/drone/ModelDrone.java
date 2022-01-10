package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelDrone extends EntityModel<EntityDroneBase> {
    private final ModelRenderer done;
    private final ModelRenderer body;
    private final ModelRenderer lower_frame_r1;
    private final ModelRenderer north_west_wing;
    private final ModelRenderer prop_1;
    private final ModelRenderer blade3_connection_r1;
    private final ModelRenderer blade2_connection_r1;
    private final ModelRenderer blade1_connection_r1;
    private final ModelRenderer south_west_wing;
    private final ModelRenderer prop_2;
    private final ModelRenderer blade6_connection_r1;
    private final ModelRenderer blade5_connection_r1;
    private final ModelRenderer blade4_connection_r1;
    private final ModelRenderer south_east_wing;
    private final ModelRenderer prop_3;
    private final ModelRenderer blade9_connection_r1;
    private final ModelRenderer blade8_connection_r1;
    private final ModelRenderer blade7_connection_r1;
    private final ModelRenderer north_east_wing;
    private final ModelRenderer prop_4;
    private final ModelRenderer blade12_connection_r1;
    private final ModelRenderer blade11_connection_r1;
    private final ModelRenderer blade10_connection_r1;

    public ModelDrone() {
        texWidth = 128;
        texHeight = 128;

        done = new ModelRenderer(this);
        done.setPos(0.0F, 22.5F, 0.0F);


        body = new ModelRenderer(this);
        body.setPos(0.0F, -3.0F, 0.0F);
        done.addChild(body);
        body.texOffs(0, 93).addBox(-4.0F, -4.0F, -12.0F, 8.0F, 4.0F, 24.0F, 0.0F, false);
        body.texOffs(10, 121).addBox(4.0F, -4.0F, 6.0F, 2.0F, 4.0F, 3.0F, 0.0F, false);
        body.texOffs(20, 121).addBox(-6.0F, -4.0F, 6.0F, 2.0F, 4.0F, 3.0F, 0.0F, false);
        body.texOffs(0, 121).addBox(4.0F, -4.0F, -9.0F, 2.0F, 4.0F, 3.0F, 0.0F, false);
        body.texOffs(30, 121).addBox(-6.0F, -4.0F, -9.0F, 2.0F, 4.0F, 3.0F, 0.0F, false);
        body.texOffs(0, 67).addBox(-4.5F, -3.5F, -12.5F, 9.0F, 1.0F, 25.0F, 0.0F, false);

        lower_frame_r1 = new ModelRenderer(this);
        lower_frame_r1.setPos(-0.25F, -0.75F, -31.25F);
        body.addChild(lower_frame_r1);
        setRotation(lower_frame_r1, -3.1416F, 0.0F, 3.1416F);
        lower_frame_r1.texOffs(0, 67).addBox(-4.75F, -0.75F, -43.75F, 9.0F, 1.0F, 25.0F, 0.0F, false);

        north_west_wing = new ModelRenderer(this);
        north_west_wing.setPos(6.0F, -5.5F, -7.5F);
        done.addChild(north_west_wing);
        setRotation(north_west_wing, 0.0F, 0.3927F, 0.0F);
        north_west_wing.texOffs(0, 113).addBox(-1.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, 0.0F, false);
        north_west_wing.texOffs(44, 110).addBox(4.5F, 1.0F, -0.5F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        north_west_wing.texOffs(52, 107).addBox(4.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        prop_1 = new ModelRenderer(this);
        prop_1.setPos(5.0F, -2.5F, 0.0F);
        north_west_wing.addChild(prop_1);
        setRotation(prop_1, 0.0F, 0.0F, 0.0F);
        prop_1.texOffs(52, 105).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        blade3_connection_r1 = new ModelRenderer(this);
        blade3_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_1.addChild(blade3_connection_r1);
        setRotation(blade3_connection_r1, 0.1572F, -0.3614F, -0.4215F);
        blade3_connection_r1.texOffs(52, 105).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade3_connection_r1.texOffs(68, 93).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade2_connection_r1 = new ModelRenderer(this);
        blade2_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_1.addChild(blade2_connection_r1);
        setRotation(blade2_connection_r1, 2.7761F, -0.7119F, -2.6117F);
        blade2_connection_r1.texOffs(48, 105).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade2_connection_r1.texOffs(54, 99).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade1_connection_r1 = new ModelRenderer(this);
        blade1_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_1.addChild(blade1_connection_r1);
        setRotation(blade1_connection_r1, -1.5708F, 1.1781F, -1.5708F);
        blade1_connection_r1.texOffs(44, 105).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade1_connection_r1.texOffs(40, 93).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        south_west_wing = new ModelRenderer(this);
        south_west_wing.setPos(6.0F, -5.5F, 7.5F);
        done.addChild(south_west_wing);
        setRotation(south_west_wing, 0.0F, -0.3927F, 0.0F);
        south_west_wing.texOffs(0, 105).addBox(-1.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, 0.0F, false);
        south_west_wing.texOffs(48, 110).addBox(4.5F, 1.0F, -0.5F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        south_west_wing.texOffs(48, 107).addBox(4.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        prop_2 = new ModelRenderer(this);
        prop_2.setPos(5.0F, -2.5F, 0.0F);
        south_west_wing.addChild(prop_2);
        setRotation(prop_2, 0.0F, 0.0F, 0.0F);
        prop_2.texOffs(48, 105).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        blade6_connection_r1 = new ModelRenderer(this);
        blade6_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_2.addChild(blade6_connection_r1);
        setRotation(blade6_connection_r1, -0.1572F, 0.3614F, -0.4215F);
        blade6_connection_r1.texOffs(48, 105).addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade6_connection_r1.texOffs(68, 99).addBox(-1.0F, -0.5F, 1.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade5_connection_r1 = new ModelRenderer(this);
        blade5_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_2.addChild(blade5_connection_r1);
        setRotation(blade5_connection_r1, -2.7761F, 0.7119F, -2.6117F);
        blade5_connection_r1.texOffs(52, 105).addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade5_connection_r1.texOffs(54, 93).addBox(-1.0F, -0.5F, 1.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade4_connection_r1 = new ModelRenderer(this);
        blade4_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_2.addChild(blade4_connection_r1);
        setRotation(blade4_connection_r1, 1.5708F, -1.1781F, -1.5708F);
        blade4_connection_r1.texOffs(40, 105).addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade4_connection_r1.texOffs(40, 99).addBox(-1.0F, -0.5F, 1.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        south_east_wing = new ModelRenderer(this);
        south_east_wing.setPos(-6.0F, -5.5F, 7.5F);
        done.addChild(south_east_wing);
        setRotation(south_east_wing, 0.0F, 0.3927F, 0.0F);
        south_east_wing.texOffs(0, 101).addBox(-6.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, 0.0F, false);
        south_east_wing.texOffs(52, 110).addBox(-5.5F, 1.0F, -0.5F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        south_east_wing.texOffs(44, 107).addBox(-5.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        prop_3 = new ModelRenderer(this);
        prop_3.setPos(-5.0F, -2.5F, 0.0F);
        south_east_wing.addChild(prop_3);
        setRotation(prop_3, 0.0F, 0.0F, 0.0F);
        prop_3.texOffs(44, 105).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        blade9_connection_r1 = new ModelRenderer(this);
        blade9_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_3.addChild(blade9_connection_r1);
        setRotation(blade9_connection_r1, -0.1572F, -0.3614F, 0.4215F);
        blade9_connection_r1.texOffs(40, 105).addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade9_connection_r1.texOffs(68, 93).addBox(-1.0F, -0.5F, 1.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade8_connection_r1 = new ModelRenderer(this);
        blade8_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_3.addChild(blade8_connection_r1);
        setRotation(blade8_connection_r1, -2.7761F, -0.7119F, 2.6117F);
        blade8_connection_r1.texOffs(52, 105).addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade8_connection_r1.texOffs(68, 99).addBox(-1.0F, -0.5F, 1.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade7_connection_r1 = new ModelRenderer(this);
        blade7_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_3.addChild(blade7_connection_r1);
        setRotation(blade7_connection_r1, 1.5708F, 1.1781F, 1.5708F);
        blade7_connection_r1.texOffs(44, 105).addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade7_connection_r1.texOffs(54, 93).addBox(-1.0F, -0.5F, 1.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        north_east_wing = new ModelRenderer(this);
        north_east_wing.setPos(-6.0F, -5.5F, -7.5F);
        done.addChild(north_east_wing);
        setRotation(north_east_wing, 0.0F, -0.3927F, 0.0F);
        north_east_wing.texOffs(0, 109).addBox(-6.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, 0.0F, false);
        north_east_wing.texOffs(40, 110).addBox(-5.5F, 1.0F, -0.5F, 1.0F, 6.0F, 1.0F, 0.0F, false);
        north_east_wing.texOffs(40, 107).addBox(-5.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        prop_4 = new ModelRenderer(this);
        prop_4.setPos(-5.0F, -2.5F, 0.0F);
        north_east_wing.addChild(prop_4);
        setRotation(prop_4, 0.0F, 0.0F, 0.0F);
        prop_4.texOffs(40, 105).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        blade12_connection_r1 = new ModelRenderer(this);
        blade12_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_4.addChild(blade12_connection_r1);
        setRotation(blade12_connection_r1, 0.1572F, 0.3614F, 0.4215F);
        blade12_connection_r1.texOffs(48, 105).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade12_connection_r1.texOffs(40, 99).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade11_connection_r1 = new ModelRenderer(this);
        blade11_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_4.addChild(blade11_connection_r1);
        setRotation(blade11_connection_r1, 2.7761F, 0.7119F, 2.6117F);
        blade11_connection_r1.texOffs(40, 105).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade11_connection_r1.texOffs(40, 93).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);

        blade10_connection_r1 = new ModelRenderer(this);
        blade10_connection_r1.setPos(0.0F, -1.0F, 0.0F);
        prop_4.addChild(blade10_connection_r1);
        setRotation(blade10_connection_r1, -1.5708F, -1.1781F, 1.5708F);
        blade10_connection_r1.texOffs(44, 105).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        blade10_connection_r1.texOffs(54, 99).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 1.0F, 5.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(EntityDroneBase drone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        done.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    @Override
    public void prepareMobModel(EntityDroneBase drone, float par2, float par3, float partialTicks) {
        float propRotation = MathHelper.lerp(partialTicks, drone.oldPropRotation, drone.propRotation);
        prop_1.yRot = propRotation;
        prop_2.yRot = propRotation;
        prop_3.yRot = -propRotation;
        prop_4.yRot = -propRotation;
    }

    @SuppressWarnings("SameParameterValue")
    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
