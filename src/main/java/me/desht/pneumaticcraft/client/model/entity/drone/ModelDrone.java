package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelDrone extends EntityModel<EntityDroneBase> {
//    private final ModelRenderer Base;
//    private final ModelRenderer Prop1Part1;
//    private final ModelRenderer Prop1Part2;
//    private final ModelRenderer Prop1Part3;
//    private final ModelRenderer Prop2Part1;
//    private final ModelRenderer Prop2Part2;
//    private final ModelRenderer Prop2Part3;
//    private final ModelRenderer Prop3Part1;
//    private final ModelRenderer Prop3Part2;
//    private final ModelRenderer Prop3Part3;
//    private final ModelRenderer Prop4Part1;
//    private final ModelRenderer Prop4Part2;
//    private final ModelRenderer Prop4Part3;
//    private final ModelRenderer Frame1;
//    private final ModelRenderer Frame2;
//    private final ModelRenderer LandingStand1;
//    private final ModelRenderer LandingStand2;
//    private final ModelRenderer LandingStand3;
//    private final ModelRenderer LandingStand4;
//    private final ModelRenderer LaserArm;
//    private final ModelRenderer LaserSource;
//
//    private float laserOffsetY = -4.5F / 16F;

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
//        textureWidth = 64;
//        textureHeight = 32;
//
//        Base = new ModelRenderer(64, 32, 0, 0);
//        Base.addBox(0F, 0F, 0F, 6, 6, 6);
//        Base.setRotationPoint(-3F, 14F, -3F);
//        Base.mirror = true;
//
//        Prop1Part1 = new ModelRenderer(64, 32, 0, 17);
//        Prop1Part1.addBox(0.5F, 0F, -0.8F, 3, 1, 2);
//        Prop1Part1.setRotationPoint(11.5F, 14F, 0F);
//        Prop1Part1.mirror = true;
//        setRotation(Prop1Part1, -0.3490659F, 0F, 0F);
//        Prop1Part2 = new ModelRenderer(64, 32, 0, 17);
//        Prop1Part2.addBox(-3.5F, 0F, -1.2F, 3, 1, 2);
//        Prop1Part2.setRotationPoint(11.5F, 14F, 0F);
//        Prop1Part2.mirror = true;
//        setRotation(Prop1Part2, 0.3490659F, 0F, 0F);
//        Prop1Part3 = new ModelRenderer(64, 32, 0, 20);
//        Prop1Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
//        Prop1Part3.setRotationPoint(11.5F, 14F, 0F);
//        Prop1Part3.mirror = true;
//        setRotation(Prop1Part3, 0F, 0F, 0F);
//        Prop2Part1 = new ModelRenderer(64, 32, 0, 17);
//        Prop2Part1.addBox(0.5F, 0F, -0.8F, 3, 1, 2);
//        Prop2Part1.setRotationPoint(-11.5F, 14F, 0F);
//        Prop2Part1.mirror = true;
//        setRotation(Prop2Part1, -0.3490659F, 0F, 0F);
//        Prop2Part2 = new ModelRenderer(64, 32, 0, 17);
//        Prop2Part2.addBox(-3.5F, 0F, -1.2F, 3, 1, 2);
//        Prop2Part2.setRotationPoint(-11.5F, 14F, 0F);
//        Prop2Part2.mirror = true;
//        setRotation(Prop2Part2, 0.3490659F, 0F, 0F);
//        Prop2Part3 = new ModelRenderer(64, 32, 0, 20);
//        Prop2Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
//        Prop2Part3.setRotationPoint(-11.5F, 14F, 0F);
//        Prop2Part3.mirror = true;
//        setRotation(Prop2Part3, 0F, 0F, 0F);
//        Prop3Part1 = new ModelRenderer(64, 32, 0, 17);
//        Prop3Part1.addBox(-3.5F, 0F, -0.8F, 3, 1, 2);
//        Prop3Part1.setRotationPoint(0F, 13.7F, -11.5F);
//        Prop3Part1.mirror = true;
//        setRotation(Prop3Part1, -0.3490659F, 0F, 0F);
//        Prop3Part2 = new ModelRenderer(64, 32, 0, 17);
//        Prop3Part2.addBox(0.5F, 0F, -1.1F, 3, 1, 2);
//        Prop3Part2.setRotationPoint(0F, 14F, -11.5F);
//        Prop3Part2.mirror = true;
//        setRotation(Prop3Part2, 0.3490659F, 0F, 0F);
//        Prop3Part3 = new ModelRenderer(64, 32, 0, 20);
//        Prop3Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
//        Prop3Part3.setRotationPoint(0F, 14F, -11.5F);
//        Prop3Part3.mirror = true;
//        setRotation(Prop3Part3, 0F, 0F, 0F);
//        Prop4Part1 = new ModelRenderer(64, 32, 0, 17);
//        Prop4Part1.addBox(-3.5F, 0F, -0.8F, 3, 1, 2);
//        Prop4Part1.setRotationPoint(0F, 14F, 11.5F);
//        Prop4Part1.mirror = true;
//        setRotation(Prop4Part1, -0.3490659F, 0F, 0F);
//        Prop4Part2 = new ModelRenderer(64, 32, 0, 17);
//        Prop4Part2.addBox(0.5F, 0F, -1.1F, 3, 1, 2);
//        Prop4Part2.setRotationPoint(0F, 14F, 11.5F);
//        Prop4Part2.mirror = true;
//        setRotation(Prop4Part2, 0.3490659F, 0F, 0F);
//        Prop4Part3 = new ModelRenderer(64, 32, 0, 20);
//        Prop4Part3.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
//        Prop4Part3.setRotationPoint(0F, 14F, 11.5F);
//        Prop4Part3.mirror = true;
//        setRotation(Prop4Part3, 0F, 0F, 0F);
//
//        Frame1 = new ModelRenderer(64, 32, 0, 26);
//        Frame1.addBox(0F, 0F, 0F, 26, 2, 2);
//        Frame1.setRotationPoint(-13F, 16F, -1F);
//        Frame1.mirror = true;
//        Frame2 = new ModelRenderer(64, 32, 0, 0);
//        Frame2.addBox(0F, 0F, 0F, 2, 2, 26);
//        Frame2.setRotationPoint(-1F, 16F, -13F);
//        Frame2.mirror = true;
//
//        LandingStand1 = new ModelRenderer(64, 32, 30, 0);
//        LandingStand1.addBox(-1F, 0F, -0.5F, 1, 6, 1);
//        LandingStand1.setRotationPoint(-8F, 18F, 0F);
//        LandingStand1.mirror = true;
//        LandingStand2 = new ModelRenderer(64, 32, 30, 0);
//        LandingStand2.addBox(0F, 0F, -0.5F, 1, 6, 1);
//        LandingStand2.setRotationPoint(8F, 18F, 0F);
//        LandingStand2.mirror = true;
//        LandingStand3 = new ModelRenderer(64, 32, 30, 0);
//        LandingStand3.addBox(-0.5F, 0F, -1F, 1, 6, 1);
//        LandingStand3.setRotationPoint(0F, 18F, -8F);
//        LandingStand3.mirror = true;
//        LandingStand4 = new ModelRenderer(64, 32, 30, 0);
//        LandingStand4.addBox(-0.5F, 0F, 0F, 1, 6, 1);
//        LandingStand4.setRotationPoint(0F, 18F, 8F);
//        LandingStand4.mirror = true;
//
//        LaserArm = new ModelRenderer(64, 32, 56, 0);
//        LaserArm.addBox(0F, 0F, 0F, 1, 2, 1);
//        LaserArm.setRotationPoint(-0.5F, 20F, -0.5F);
//        LaserArm.mirror = true;
//        LaserSource = new ModelRenderer(64, 32, 56, 3);
//        LaserSource.addBox(0F, 0F, 0F, 2, 2, 2);
//        LaserSource.setRotationPoint(-1F, 22F, -1F);
//        LaserSource.mirror = true;
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
//        Base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//
//        Prop1Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop1Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop1Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop2Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop2Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop2Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop3Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop3Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop3Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop4Part1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop4Part2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Prop4Part3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Frame1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        Frame2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        LandingStand1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        LandingStand2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        LandingStand3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        LandingStand4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//
//        matrixStackIn.translate(0, laserOffsetY, 0);
//        LaserArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        LaserSource.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//        matrixStackIn.translate(0, -laserOffsetY, 0);
        done.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    @Override
    public void prepareMobModel(EntityDroneBase drone, float par2, float par3, float partialTicks) {
//        float propRotation = MathHelper.lerp(partialTicks, drone.oldPropRotation, drone.propRotation);
//        Prop1Part1.rotateAngleY = propRotation;
//        Prop1Part2.rotateAngleY = propRotation;
//        Prop1Part3.rotateAngleY = propRotation;
//        Prop2Part1.rotateAngleY = propRotation;
//        Prop2Part2.rotateAngleY = propRotation;
//        Prop2Part3.rotateAngleY = propRotation;
//        Prop3Part1.rotateAngleY = -propRotation;
//        Prop3Part2.rotateAngleY = -propRotation;
//        Prop3Part3.rotateAngleY = -propRotation;
//        Prop4Part1.rotateAngleY = -propRotation;
//        Prop4Part2.rotateAngleY = -propRotation;
//        Prop4Part3.rotateAngleY = -propRotation;
//
//        float laserExtension = MathHelper.lerp(partialTicks, drone.oldLaserExtension, drone.laserExtension);
//        laserOffsetY = (1F - laserExtension) * -4.5F / 16F;

        float propRotation = MathHelper.lerp(partialTicks, drone.oldPropRotation, drone.propRotation);
        prop_1.rotateAngleY = propRotation;
        prop_2.rotateAngleY = propRotation;
        prop_3.rotateAngleY = -propRotation;
        prop_4.rotateAngleY = -propRotation;
    }

    @SuppressWarnings("SameParameterValue")
    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
