package pneumaticCraft.common.thirdparty.ic2;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.lib.Textures;

public class ModelPneumaticGenerator extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer base1;
    ModelRenderer base2;
    ModelRenderer front;
    ModelRenderer back;
    ModelRenderer cyl1;
    ModelRenderer cyl2;
    ModelRenderer cyl3;
    ModelRenderer cyl4;
    ModelRenderer cyl5;

    public ModelPneumaticGenerator(){
        textureWidth = 128;
        textureHeight = 128;

        base1 = new ModelRenderer(this, 0, 0);
        base1.addBox(0F, 0F, 0F, 16, 2, 16);
        base1.setRotationPoint(-8F, 22F, -8F);
        base1.setTextureSize(128, 128);
        base1.mirror = true;
        setRotation(base1, 0F, 0F, 0F);
        base2 = new ModelRenderer(this, 52, 0);
        base2.addBox(0F, 0F, 0F, 10, 1, 10);
        base2.setRotationPoint(-5F, 21F, -3F);
        base2.setTextureSize(128, 128);
        base2.mirror = true;
        setRotation(base2, 0F, 0F, 0F);
        front = new ModelRenderer(this, 0, 39);
        front.addBox(0F, 0F, 0F, 12, 11, 1);
        front.setRotationPoint(-6F, 11F, 7F);
        front.setTextureSize(128, 128);
        front.mirror = true;
        setRotation(front, 0F, 0F, 0F);
        back = new ModelRenderer(this, 0, 19);
        back.addBox(0F, 0F, 0F, 16, 14, 5);
        back.setRotationPoint(-8F, 8F, -8F);
        back.setTextureSize(128, 128);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);
        cyl1 = new ModelRenderer(this, 95, 0);
        cyl1.addBox(-3F, -6F, 0F, 6, 1, 10);
        cyl1.setRotationPoint(0F, 17F, -3F);
        cyl1.setTextureSize(128, 128);
        cyl1.mirror = true;
        setRotation(cyl1, 0F, 0F, 0F);
        cyl2 = new ModelRenderer(this, 95, 0);
        cyl2.addBox(-3F, -6F, 0F, 6, 1, 10);
        cyl2.setRotationPoint(0F, 17F, -3F);
        cyl2.setTextureSize(128, 128);
        cyl2.mirror = true;
        setRotation(cyl2, 0F, 0F, 0.9424778F);
        cyl3 = new ModelRenderer(this, 95, 0);
        cyl3.addBox(-3F, -6F, 0F, 6, 1, 10);
        cyl3.setRotationPoint(0F, 17F, -3F);
        cyl3.setTextureSize(128, 128);
        cyl3.mirror = true;
        setRotation(cyl3, 0F, 0F, -0.9424778F);
        cyl4 = new ModelRenderer(this, 95, 0);
        cyl4.addBox(-3F, -6F, 0F, 6, 1, 10);
        cyl4.setRotationPoint(0F, 17F, -3F);
        cyl4.setTextureSize(128, 128);
        cyl4.mirror = true;
        setRotation(cyl4, 0F, 0F, 1.884956F);
        cyl5 = new ModelRenderer(this, 95, 0);
        cyl5.addBox(-3F, -6F, 0F, 6, 1, 10);
        cyl5.setRotationPoint(0F, 17F, -3F);
        cyl5.setTextureSize(128, 128);
        cyl5.mirror = true;
        setRotation(cyl5, 0F, 0F, -1.884956F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        base1.render(f5);
        base2.render(f5);
        front.render(f5);
        back.render(f5);
        cyl1.render(f5);
        cyl2.render(f5);
        cyl3.render(f5);
        cyl4.render(f5);
        cyl5.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_PNEUMATIC_GENERATOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        base1.render(size);
        base2.render(size);
        front.render(size);
        back.render(size);
        cyl1.render(size);
        cyl2.render(size);
        cyl3.render(size);
        cyl4.render(size);
        cyl5.render(size);
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        // TODO Auto-generated method stub

    }
}
