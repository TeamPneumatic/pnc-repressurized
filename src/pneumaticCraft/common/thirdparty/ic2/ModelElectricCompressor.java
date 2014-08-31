package pneumaticCraft.common.thirdparty.ic2;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.lib.Textures;

public class ModelElectricCompressor extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer base;
    ModelRenderer frontEU;
    ModelRenderer frontMJ;
    ModelRenderer frontRF;
    ModelRenderer back;
    ModelRenderer pipe1;
    ModelRenderer pipe2;
    ModelRenderer pipe3;
    ModelRenderer pipe4;
    ModelRenderer drum1;
    ModelRenderer drum2;
    ModelRenderer drum3;
    ModelRenderer drum4;
    ModelRenderer drumTop;
    ModelRenderer cyl1;
    ModelRenderer cyl2;
    ModelRenderer cyl3;
    ModelRenderer cyl4;
    ModelRenderer cyl5;
    ModelRenderer cyl6;
    ModelRenderer cyl7;
    ModelRenderer cyl8;

    public ModelElectricCompressor(){
        textureWidth = 128;
        textureHeight = 128;

        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 16, 1, 16);
        base.setRotationPoint(-8F, 23F, -8F);
        base.setTextureSize(128, 128);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        frontEU = new ModelRenderer(this, 50, 0);
        frontEU.addBox(0F, 0F, 0F, 12, 13, 3);
        frontEU.setRotationPoint(-6F, 10F, -8F);
        frontEU.setTextureSize(128, 128);
        frontEU.mirror = true;
        setRotation(frontEU, 0F, 0F, 0F);
        frontMJ = new ModelRenderer(this, 50, 35);
        frontMJ.addBox(0F, 0F, 0F, 12, 13, 3);
        frontMJ.setRotationPoint(-6F, 10F, -8F);
        frontMJ.setTextureSize(128, 128);
        frontMJ.mirror = true;
        setRotation(frontMJ, 0F, 0F, 0F);
        frontRF = new ModelRenderer(this, 50, 53);
        frontRF.addBox(0F, 0F, 0F, 12, 13, 3);
        frontRF.setRotationPoint(-6F, 10F, -8F);
        frontRF.setTextureSize(128, 128);
        frontRF.mirror = true;
        setRotation(frontRF, 0F, 0F, 0F);
        back = new ModelRenderer(this, 81, 0);
        back.addBox(0F, 0F, 0F, 10, 12, 1);
        back.setRotationPoint(-5F, 11F, 6F);
        back.setTextureSize(128, 128);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);
        pipe1 = new ModelRenderer(this, 9, 0);
        pipe1.addBox(0F, 0F, 0F, 1, 2, 2);
        pipe1.setRotationPoint(1F, 15F, 6F);
        pipe1.setTextureSize(128, 128);
        pipe1.mirror = true;
        setRotation(pipe1, 0F, 0F, 0F);
        pipe2 = new ModelRenderer(this, 9, 5);
        pipe2.addBox(0F, 0F, 0F, 1, 2, 2);
        pipe2.setRotationPoint(-2F, 15F, 6F);
        pipe2.setTextureSize(128, 128);
        pipe2.mirror = true;
        setRotation(pipe2, 0F, 0F, 0F);
        pipe3 = new ModelRenderer(this, 0, 5);
        pipe3.addBox(0F, 0F, 0F, 2, 1, 2);
        pipe3.setRotationPoint(-1F, 17F, 6F);
        pipe3.setTextureSize(128, 128);
        pipe3.mirror = true;
        setRotation(pipe3, 0F, 0F, 0F);
        pipe4 = new ModelRenderer(this, 0, 0);
        pipe4.addBox(0F, 0F, 0F, 2, 1, 2);
        pipe4.setRotationPoint(-1F, 14F, 6F);
        pipe4.setTextureSize(128, 128);
        pipe4.mirror = true;
        setRotation(pipe4, 0F, 0F, 0F);
        drum1 = new ModelRenderer(this, 107, 0);
        drum1.addBox(-1.5F, 0F, -3F, 3, 14, 6);
        drum1.setRotationPoint(-5F, 9F, 2F);
        drum1.setTextureSize(128, 128);
        drum1.mirror = true;
        setRotation(drum1, 0F, 1.570796F, 0F);
        drum2 = new ModelRenderer(this, 107, 0);
        drum2.addBox(-1.5F, 0F, -3F, 3, 14, 6);
        drum2.setRotationPoint(-5F, 9F, 2F);
        drum2.setTextureSize(128, 128);
        drum2.mirror = true;
        setRotation(drum2, 0F, 0F, 0F);
        drum3 = new ModelRenderer(this, 0, 20);
        drum3.addBox(-1F, 0F, -3F, 2, 14, 6);
        drum3.setRotationPoint(-5F, 9F, 2F);
        drum3.setTextureSize(128, 128);
        drum3.mirror = true;
        setRotation(drum3, 0F, -0.7853982F, 0F);
        drum4 = new ModelRenderer(this, 0, 20);
        drum4.addBox(-1F, 0F, -3F, 2, 14, 6);
        drum4.setRotationPoint(-5F, 9F, 2F);
        drum4.setTextureSize(128, 128);
        drum4.mirror = true;
        setRotation(drum4, 0F, 0.7853982F, 0F);
        drumTop = new ModelRenderer(this, 57, 20);
        drumTop.addBox(0F, 0F, 0F, 4, 1, 4);
        drumTop.setRotationPoint(-7F, 8F, 0F);
        drumTop.setTextureSize(128, 128);
        drumTop.mirror = true;
        setRotation(drumTop, 0F, 0F, 0F);
        cyl1 = new ModelRenderer(this, 20, 20);
        cyl1.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl1.setRotationPoint(0F, 16F, -5F);
        cyl1.setTextureSize(128, 128);
        cyl1.mirror = true;
        setRotation(cyl1, 0F, 0F, 0F);
        cyl2 = new ModelRenderer(this, 20, 20);
        cyl2.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl2.setRotationPoint(0F, 16F, -5F);
        cyl2.setTextureSize(128, 128);
        cyl2.mirror = true;
        setRotation(cyl2, 0F, 0F, 0.7853982F);
        cyl3 = new ModelRenderer(this, 20, 20);
        cyl3.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl3.setRotationPoint(0F, 16F, -5F);
        cyl3.setTextureSize(128, 128);
        cyl3.mirror = true;
        setRotation(cyl3, 0F, 0F, 1.570796F);
        cyl4 = new ModelRenderer(this, 20, 20);
        cyl4.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl4.setRotationPoint(0F, 16F, -5F);
        cyl4.setTextureSize(128, 128);
        cyl4.mirror = true;
        setRotation(cyl4, 0F, 0F, 2.356194F);
        cyl5 = new ModelRenderer(this, 20, 20);
        cyl5.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl5.setRotationPoint(0F, 16F, -5F);
        cyl5.setTextureSize(128, 128);
        cyl5.mirror = true;
        setRotation(cyl5, 0F, 0F, 3.141593F);
        cyl6 = new ModelRenderer(this, 20, 20);
        cyl6.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl6.setRotationPoint(0F, 16F, -5F);
        cyl6.setTextureSize(128, 128);
        cyl6.mirror = true;
        setRotation(cyl6, 0F, 0F, -2.356194F);
        cyl7 = new ModelRenderer(this, 20, 20);
        cyl7.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl7.setRotationPoint(0F, 16F, -5F);
        cyl7.setTextureSize(128, 128);
        cyl7.mirror = true;
        setRotation(cyl7, 0F, 0F, -1.570796F);
        cyl8 = new ModelRenderer(this, 20, 20);
        cyl8.addBox(-2F, -5F, 0F, 4, 1, 11);
        cyl8.setRotationPoint(0F, 16F, -5F);
        cyl8.setTextureSize(128, 128);
        cyl8.mirror = true;
        setRotation(cyl8, 0F, 0F, -0.7853982F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        base.render(f5);
        frontEU.render(f5);
        frontMJ.render(f5);
        frontRF.render(f5);
        back.render(f5);
        pipe1.render(f5);
        pipe2.render(f5);
        pipe3.render(f5);
        pipe4.render(f5);
        drum1.render(f5);
        drum2.render(f5);
        drum3.render(f5);
        drum4.render(f5);
        drumTop.render(f5);
        cyl1.render(f5);
        cyl2.render(f5);
        cyl3.render(f5);
        cyl4.render(f5);
        cyl5.render(f5);
        cyl6.render(f5);
        cyl7.render(f5);
        cyl8.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        base.render(size);
        frontEU.render(size);
        frontMJ.render(size);
        frontRF.render(size);
        back.render(size);
        pipe1.render(size);
        pipe2.render(size);
        pipe3.render(size);
        pipe4.render(size);
        drum1.render(size);
        drum2.render(size);
        drum3.render(size);
        drum4.render(size);
        drumTop.render(size);
        cyl1.render(size);
        cyl2.render(size);
        cyl3.render(size);
        cyl4.render(size);
        cyl5.render(size);
        cyl6.render(size);
        cyl7.render(size);
        cyl8.render(size);
    }

    @Override
    public ResourceLocation getModelTexture(){
        return Textures.MODEL_ELECTRIC_COMPRESSOR;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        // TODO Auto-generated method stub

    }

}
