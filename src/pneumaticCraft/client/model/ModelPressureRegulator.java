package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

public class ModelPressureRegulator extends ModelBase{
    // fields
    ModelRenderer Shape1;
    ModelRenderer Valve;

    public ModelPressureRegulator(){
        textureWidth = 64;
        textureHeight = 32;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 7, 7, 7);
        Shape1.setRotationPoint(-3.5F, 12.5F, -3.5F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Valve = new ModelRenderer(this, 0, 20);
        Valve.addBox(0F, 0F, 0F, 3, 8, 4);
        Valve.setRotationPoint(-1.5F, 12F, 0F);
        Valve.setTextureSize(64, 32);
        Valve.mirror = true;
        setRotation(Valve, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Valve.render(f5);
    }

    public void renderModel(float size, float shift){
        Shape1.render(size);
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, shift);
        Valve.render(size);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glRotatef(180, 0, 1, 0);
        GL11.glTranslated(0, 0, shift);
        Valve.render(size);

        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
