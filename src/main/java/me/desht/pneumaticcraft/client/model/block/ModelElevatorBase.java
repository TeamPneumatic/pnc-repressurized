package me.desht.pneumaticcraft.client.model.block;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class ModelElevatorBase extends ModelBase {
    private final ModelRenderer pole1;
    private final ModelRenderer pole2;
    private final ModelRenderer pole3;
    private final ModelRenderer pole4;
    private final ModelRenderer floor;

    public ModelElevatorBase() {
        textureWidth = 64;
        textureHeight = 64;

        pole1 = new ModelRenderer(this, 0, 17);
        pole1.addBox(0F, 0F, 0F, 2, 14, 2);
        pole1.setRotationPoint(-1F, 9F, -1F);
        pole1.setTextureSize(64, 64);
        pole1.mirror = true;
        setRotation(pole1, 0F, 0F, 0F);
        pole2 = new ModelRenderer(this, 0, 17);
        pole2.addBox(0F, 0F, 0F, 4, 14, 4);
        pole2.setRotationPoint(-2F, 9F, -2F);
        pole2.setTextureSize(64, 64);
        pole2.mirror = true;
        setRotation(pole2, 0F, 0F, 0F);
        pole3 = new ModelRenderer(this, 0, 17);
        pole3.addBox(0F, 0F, 0F, 6, 14, 6);
        pole3.setRotationPoint(-3F, 9F, -3F);
        pole3.setTextureSize(64, 64);
        pole3.mirror = true;
        setRotation(pole3, 0F, 0F, 0F);
        pole4 = new ModelRenderer(this, 0, 17);
        pole4.addBox(0F, 0F, 0F, 8, 14, 8);
        pole4.setRotationPoint(-4F, 9F, -4F);
        pole4.setTextureSize(64, 64);
        pole4.mirror = true;
        setRotation(pole4, 0F, 0F, 0F);
        floor = new ModelRenderer(this, 0, 0);
        floor.addBox(0F, 0F, 0F, 16, 1, 16);
        floor.setRotationPoint(-8F, 8F, -8F);
        floor.setTextureSize(64, 64);
        floor.mirror = true;
        setRotation(floor, 0F, 0F, 0F);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    private static final float FACTOR = 9F / 16;

    public void renderModel(float scale, float extension) {
        renderPole(pole4, 0, scale, extension);
        renderPole(pole3, 1, scale, extension);
        renderPole(pole2, 2, scale, extension);
        renderPole(pole1, 3, scale, extension);
        GlStateManager.color(1, 1, 1,1 );
        floor.render(scale);
    }

    private void renderPole(ModelRenderer pole, int idx, float scale, float extension) {
        GlStateManager.translate(0, -extension / 4, 0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, FACTOR, 0);
        GlStateManager.scale(1, extension * 16 / 14 / 4, 1);
        GlStateManager.translate(0, -FACTOR, 0);
        GlStateManager.color(1 - idx * 0.15f, 1 - idx * 0.15f, 1 - idx * 0.15f, 1);
        pole.render(scale);
        GlStateManager.popMatrix();
    }
}
