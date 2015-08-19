package pneumaticCraft.client.model.semiblocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelHeatFrame extends ModelBase
{
  //fields
    ModelRenderer Bottom3;
    ModelRenderer Bottom4;
    ModelRenderer Bottom5;
    ModelRenderer Bottom6;
    ModelRenderer Bottom7;
    ModelRenderer Bottom8;
    ModelRenderer Bottom2;
    ModelRenderer Bottom1;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Top6;
    ModelRenderer Top3;
    ModelRenderer Top4;
    ModelRenderer Top8;
    ModelRenderer Top1;
    ModelRenderer Top5;
    ModelRenderer Top2;
    ModelRenderer Top7;
  
  public ModelHeatFrame()
  {
    textureWidth = 64;
    textureHeight = 64;
    
      Bottom3 = new ModelRenderer(this, 0, 0);
      Bottom3.addBox(0F, 0F, 0F, 4, 4, 4);
      Bottom3.setRotationPoint(-8.5F, 20.5F, 4.5F);
      Bottom3.setTextureSize(64, 64);
      Bottom3.mirror = true;
      setRotation(Bottom3, 0F, 0F, 0F);
      Bottom4 = new ModelRenderer(this, 16, 0);
      Bottom4.addBox(0F, 0F, 0F, 4, 4, 4);
      Bottom4.setRotationPoint(4.5F, 20.5F, 4.5F);
      Bottom4.setTextureSize(64, 64);
      Bottom4.mirror = true;
      setRotation(Bottom4, 0F, 0F, 0F);
      Bottom5 = new ModelRenderer(this, 32, 0);
      Bottom5.addBox(0F, 0F, 0F, 9, 1, 1);
      Bottom5.setRotationPoint(-4.5F, 23.5F, -8.5F);
      Bottom5.setTextureSize(64, 64);
      Bottom5.mirror = true;
      setRotation(Bottom5, 0F, 0F, 0F);
      Bottom6 = new ModelRenderer(this, 32, 2);
      Bottom6.addBox(0F, 0F, 0F, 9, 1, 1);
      Bottom6.setRotationPoint(-4.5F, 23.5F, 7.5F);
      Bottom6.setTextureSize(64, 64);
      Bottom6.mirror = true;
      setRotation(Bottom6, 0F, 0F, 0F);
      Bottom7 = new ModelRenderer(this, 44, 0);
      Bottom7.addBox(0F, 0F, 0F, 1, 1, 9);
      Bottom7.setRotationPoint(-8.5F, 23.5F, -4.5F);
      Bottom7.setTextureSize(64, 64);
      Bottom7.mirror = true;
      setRotation(Bottom7, 0F, 0F, 0F);
      Bottom8 = new ModelRenderer(this, 32, 4);
      Bottom8.addBox(0F, 0F, 0F, 1, 1, 9);
      Bottom8.setRotationPoint(7.5F, 23.5F, -4.5F);
      Bottom8.setTextureSize(64, 64);
      Bottom8.mirror = true;
      setRotation(Bottom8, 0F, 0F, 0F);
      Bottom2 = new ModelRenderer(this, 0, 8);
      Bottom2.addBox(0F, 0F, 0F, 4, 4, 4);
      Bottom2.setRotationPoint(-8.5F, 20.5F, -8.5F);
      Bottom2.setTextureSize(64, 64);
      Bottom2.mirror = true;
      setRotation(Bottom2, 0F, 0F, 0F);
      Bottom1 = new ModelRenderer(this, 16, 8);
      Bottom1.addBox(0F, 0F, 0F, 4, 4, 4);
      Bottom1.setRotationPoint(4.5F, 20.5F, -8.5F);
      Bottom1.setTextureSize(64, 64);
      Bottom1.mirror = true;
      setRotation(Bottom1, 0F, 0F, 0F);
      Shape1 = new ModelRenderer(this, 0, 16);
      Shape1.addBox(0F, 0F, 0F, 1, 9, 1);
      Shape1.setRotationPoint(-8.5F, 11.5F, -8.5F);
      Shape1.setTextureSize(64, 64);
      Shape1.mirror = true;
      setRotation(Shape1, 0F, 0F, 0F);
      Shape2 = new ModelRenderer(this, 4, 16);
      Shape2.addBox(0F, 0F, 0F, 1, 9, 1);
      Shape2.setRotationPoint(7.5F, 11.5F, -8.5F);
      Shape2.setTextureSize(64, 64);
      Shape2.mirror = true;
      setRotation(Shape2, 0F, 0F, 0F);
      Shape3 = new ModelRenderer(this, 0, 16);
      Shape3.addBox(0F, 0F, 0F, 1, 9, 1);
      Shape3.setRotationPoint(-8.5F, 11.5F, 7.5F);
      Shape3.setTextureSize(64, 64);
      Shape3.mirror = true;
      setRotation(Shape3, 0F, 0F, 0F);
      Shape4 = new ModelRenderer(this, 0, 16);
      Shape4.addBox(0F, 0F, 0F, 1, 9, 1);
      Shape4.setRotationPoint(7.5F, 11.5F, 7.5F);
      Shape4.setTextureSize(64, 64);
      Shape4.mirror = true;
      setRotation(Shape4, 0F, 0F, 0F);
      Top6 = new ModelRenderer(this, 32, 26);
      Top6.addBox(0F, 0F, 0F, 9, 1, 1);
      Top6.setRotationPoint(-4.5F, 7.5F, -8.5F);
      Top6.setTextureSize(64, 64);
      Top6.mirror = true;
      setRotation(Top6, 0F, 0F, 0F);
      Top3 = new ModelRenderer(this, 0, 34);
      Top3.addBox(0F, 0F, 0F, 4, 4, 4);
      Top3.setRotationPoint(-8.5F, 7.5F, -8.5F);
      Top3.setTextureSize(64, 64);
      Top3.mirror = true;
      setRotation(Top3, 0F, 0F, 0F);
      Top4 = new ModelRenderer(this, 16, 26);
      Top4.addBox(0F, 0F, 0F, 4, 4, 4);
      Top4.setRotationPoint(4.5F, 7.5F, 4.5F);
      Top4.setTextureSize(64, 64);
      Top4.mirror = true;
      setRotation(Top4, 0F, 0F, 0F);
      Top8 = new ModelRenderer(this, 32, 30);
      Top8.addBox(0F, 0F, 0F, 1, 1, 9);
      Top8.setRotationPoint(-8.5F, 7.5F, -4.5F);
      Top8.setTextureSize(64, 64);
      Top8.mirror = true;
      setRotation(Top8, 0F, 0F, 0F);
      Top1 = new ModelRenderer(this, 0, 26);
      Top1.addBox(0F, 0F, 0F, 4, 4, 4);
      Top1.setRotationPoint(-8.5F, 7.5F, 4.5F);
      Top1.setTextureSize(64, 64);
      Top1.mirror = true;
      setRotation(Top1, 0F, 0F, 0F);
      Top5 = new ModelRenderer(this, 32, 28);
      Top5.addBox(0F, 0F, 0F, 9, 1, 1);
      Top5.setRotationPoint(-4.5F, 7.5F, 7.5F);
      Top5.setTextureSize(64, 64);
      Top5.mirror = true;
      setRotation(Top5, 0F, 0F, 0F);
      Top2 = new ModelRenderer(this, 16, 34);
      Top2.addBox(0F, 0F, 0F, 4, 4, 4);
      Top2.setRotationPoint(4.5F, 7.5F, -8.5F);
      Top2.setTextureSize(64, 64);
      Top2.mirror = true;
      setRotation(Top2, 0F, 0F, 0F);
      Top7 = new ModelRenderer(this, 44, 26);
      Top7.addBox(0F, 0F, 0F, 1, 1, 9);
      Top7.setRotationPoint(7.5F, 7.5F, -4.5F);
      Top7.setTextureSize(64, 64);
      Top7.mirror = true;
      setRotation(Top7, 0F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5,entity);
    Bottom3.render(f5);
    Bottom4.render(f5);
    Bottom5.render(f5);
    Bottom6.render(f5);
    Bottom7.render(f5);
    Bottom8.render(f5);
    Bottom2.render(f5);
    Bottom1.render(f5);
    Shape1.render(f5);
    Shape2.render(f5);
    Shape3.render(f5);
    Shape4.render(f5);
    Top6.render(f5);
    Top3.render(f5);
    Top4.render(f5);
    Top8.render(f5);
    Top1.render(f5);
    Top5.render(f5);
    Top2.render(f5);
    Top7.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
}
