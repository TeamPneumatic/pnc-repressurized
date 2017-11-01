package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.opengl.GL11;

public class ModelAirCannon extends AbstractModelRenderer.BaseModel {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseFrame1;
    private final ModelRenderer baseFrame2;
    private final ModelRenderer baseFrame3;
    private final ModelRenderer baseFrame4;
    private final ModelRenderer baseFrame5;
    private final ModelRenderer baseFrame6;
    private final ModelRenderer cannon1;
    private final ModelRenderer cannon2;
    private final ModelRenderer cannon3;
    private final ModelRenderer cannon4;
    private final ModelRenderer cannon5;

    public ModelAirCannon() {
        baseTurn = new ModelRenderer(this, 36, 7);
        baseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        baseTurn.setRotationPoint(-3.5F, 20F, -5F);
        baseTurn.setTextureSize(64, 32);
        baseTurn.mirror = true;
        setRotation(baseTurn, 0F, 0F, 0F);

        baseFrame1 = new ModelRenderer(this, 10, 7);
        baseFrame1.addBox(0F, 0F, 0F, 1, 5, 3);
        baseFrame1.setRotationPoint(-3.5F, 15F, -3F);
        baseFrame1.setTextureSize(64, 32);
        baseFrame1.mirror = true;
        setRotation(baseFrame1, 0F, 0F, 0F);
        baseFrame2 = new ModelRenderer(this, 10, 7);
        baseFrame2.addBox(0F, 0F, 0F, 1, 5, 3);
        baseFrame2.setRotationPoint(2.5F, 15F, -3F);
        baseFrame2.setTextureSize(64, 32);
        baseFrame2.mirror = true;
        setRotation(baseFrame2, 0F, 0F, 0F);
        baseFrame3 = new ModelRenderer(this, 18, 13);
        baseFrame3.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame3.setRotationPoint(-3.5F, 14F, -2F);
        baseFrame3.setTextureSize(64, 32);
        baseFrame3.mirror = true;
        setRotation(baseFrame3, 0F, 0F, 0F);
        baseFrame4 = new ModelRenderer(this, 18, 13);
        baseFrame4.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame4.setRotationPoint(2.5F, 14F, -2F);
        baseFrame4.setTextureSize(64, 32);
        baseFrame4.mirror = true;
        setRotation(baseFrame4, 0F, 0F, 0F);
        baseFrame5 = new ModelRenderer(this, 19, 10);
        baseFrame5.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame5.setRotationPoint(2F, 15.5F, -2F);
        baseFrame5.setTextureSize(64, 32);
        baseFrame5.mirror = true;
        setRotation(baseFrame5, 0F, 0F, 0F);
        baseFrame6 = new ModelRenderer(this, 19, 10);
        baseFrame6.addBox(0F, 0F, 0F, 1, 1, 1);
        baseFrame6.setRotationPoint(-3F, 15.5F, -2F);
        baseFrame6.setTextureSize(64, 32);
        baseFrame6.mirror = true;
        setRotation(baseFrame6, 0F, 0F, 0F);

        cannon1 = new ModelRenderer(this, 24, 0);
        cannon1.addBox(0F, 3F, 0F, 2, 1, 2);
        cannon1.setRotationPoint(-1F, 15F, -2.5F);
        cannon1.setTextureSize(64, 32);
        cannon1.mirror = true;
        setRotation(cannon1, 0F, 0F, 0F);
        cannon2 = new ModelRenderer(this, 27, 3);
        cannon2.addBox(0F, 0F, 0F, 2, 8, 1);
        cannon2.setRotationPoint(-1F, 10F, -0.5F);
        cannon2.setTextureSize(64, 32);
        cannon2.mirror = true;
        setRotation(cannon2, 0F, 0F, 0F);
        cannon3 = new ModelRenderer(this, 27, 3);
        cannon3.addBox(0F, 0F, 0F, 2, 8, 1);
        cannon3.setRotationPoint(-1F, 10F, -3.5F);
        cannon3.setTextureSize(64, 32);
        cannon3.mirror = true;
        setRotation(cannon3, 0F, 0F, 0F);
        cannon4 = new ModelRenderer(this, 18, 0);
        cannon4.addBox(0F, 0F, 0F, 1, 8, 2);
        cannon4.setRotationPoint(-2F, 10F, -2.5F);
        cannon4.setTextureSize(64, 32);
        cannon4.mirror = true;
        setRotation(cannon4, 0F, 0F, 0F);
        cannon5 = new ModelRenderer(this, 18, 0);
        cannon5.addBox(0F, 0F, 0F, 1, 8, 2);
        cannon5.setRotationPoint(1F, 10F, -2.5F);
        cannon5.setTextureSize(64, 32);
        cannon5.mirror = true;
        setRotation(cannon5, 0F, 0F, 0F);
    }

//    @Override
//    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
//        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
//
//        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
//        baseTurn.render(scale);
//        baseFrame1.render(scale);
//        baseFrame2.render(scale);
//        baseFrame3.render(scale);
//        baseFrame4.render(scale);
//        baseFrame5.render(scale);
//        baseFrame6.render(scale);
//        cannon1.render(scale);
//        cannon2.render(scale);
//        cannon3.render(scale);
//        cannon4.render(scale);
//        cannon5.render(scale);
//    }

    public void renderModel(float size, float rotationAngle, float heightAngle) {
        GL11.glPushMatrix();

        GL11.glTranslated(0.0, 0.0, -0.09375D);
        GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);
        GL11.glTranslated(0.0, 0.0, 0.09375D);
        baseTurn.render(size);
        baseFrame1.render(size);
        baseFrame2.render(size);
        baseFrame3.render(size);
        baseFrame4.render(size);
        baseFrame5.render(size);
        baseFrame6.render(size);

        GL11.glPushMatrix();
        GL11.glTranslated(0.0D, 1.0D, -0.09375D);
        GL11.glRotatef(heightAngle, 1.0F, 0.0F, 0.0F);
        GL11.glTranslated(0.0D, -1.0D, 0.09375D);
        cannon1.render(size);
        cannon2.render(size);
        cannon3.render(size);
        cannon4.render(size);
        cannon5.render(size);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }
}
