package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelCropSupport extends Model {
    //fields
    private final RendererModel Shape1;
    private final RendererModel Shape2;
    private final RendererModel Shape3;
    private final RendererModel Shape4;

    public ModelCropSupport() {
        textureWidth = 64;
        textureHeight = 64;

        Shape1 = new RendererModel(this, 0, 16);
        Shape1.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape1.setRotationPoint(-8.5F, 11.5F, -8.5F);
        Shape1.setTextureSize(64, 64);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new RendererModel(this, 4, 16);
        Shape2.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape2.setRotationPoint(7.5F, 11.5F, -8.5F);
        Shape2.setTextureSize(64, 64);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape3 = new RendererModel(this, 0, 16);
        Shape3.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape3.setRotationPoint(-8.5F, 11.5F, 7.5F);
        Shape3.setTextureSize(64, 64);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F);
        Shape4 = new RendererModel(this, 0, 16);
        Shape4.addBox(0F, 0F, 0F, 1, 9, 1);
        Shape4.setRotationPoint(7.5F, 11.5F, 7.5F);
        Shape4.setTextureSize(64, 64);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
    }

//    @Override
    public void render(float scale) {
//        super.render(entity, f, f1, f2, f3, f4, f5);
//        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(scale);
        Shape2.render(scale);
        Shape3.render(scale);
        Shape4.render(scale);
    }

    private void setRotation(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
