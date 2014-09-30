package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityVacuumPump;
import pneumaticCraft.lib.Textures;

public class ModelVacuumPump extends ModelBase implements IBaseModel{
    // fields
    ModelRenderer Left1;
    ModelRenderer Left2;
    ModelRenderer Left3;
    ModelRenderer Left4;
    ModelRenderer Right1;
    ModelRenderer Right2;
    ModelRenderer Right3;
    ModelRenderer Right4;
    ModelRenderer TurbineCase;
    ModelRenderer Base;
    ModelRenderer Platform;
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;
    ModelRenderer Top;
    ModelRenderer Blade;

    public ModelVacuumPump(){
        textureWidth = 64;
        textureHeight = 64;

        Left1 = new ModelRenderer(this, 0, 0);
        Left1.addBox(0F, 0F, 0F, 6, 1, 2);
        Left1.setRotationPoint(2F, 14F, -1F);
        Left1.setTextureSize(64, 64);
        Left1.mirror = true;
        setRotation(Left1, 0F, 0F, 0F);
        Left2 = new ModelRenderer(this, 0, 0);
        Left2.addBox(0F, 0F, 0F, 6, 1, 2);
        Left2.setRotationPoint(2F, 17F, -1F);
        Left2.setTextureSize(64, 64);
        Left2.mirror = true;
        setRotation(Left2, 0F, 0F, 0F);
        Left3 = new ModelRenderer(this, 0, 0);
        Left3.addBox(0F, 0F, 0F, 5, 2, 1);
        Left3.setRotationPoint(3F, 15F, -2F);
        Left3.setTextureSize(64, 64);
        Left3.mirror = true;
        setRotation(Left3, 0F, 0F, 0F);
        Left4 = new ModelRenderer(this, 0, 0);
        Left4.addBox(0F, 0F, 0F, 5, 2, 1);
        Left4.setRotationPoint(3F, 15F, 1F);
        Left4.setTextureSize(64, 64);
        Left4.mirror = true;
        setRotation(Left4, 0F, 0F, 0F);
        Right1 = new ModelRenderer(this, 0, 0);
        Right1.addBox(0F, 0F, 0F, 6, 1, 2);
        Right1.setRotationPoint(-8F, 14F, -1F);
        Right1.setTextureSize(64, 64);
        Right1.mirror = true;
        setRotation(Right1, 0F, 0F, 0F);
        Right2 = new ModelRenderer(this, 0, 0);
        Right2.addBox(0F, 0F, 0F, 6, 1, 2);
        Right2.setRotationPoint(-8F, 17F, -1F);
        Right2.setTextureSize(64, 64);
        Right2.mirror = true;
        setRotation(Right2, 0F, 0F, 0F);
        Right3 = new ModelRenderer(this, 0, 0);
        Right3.addBox(0F, 0F, 0F, 5, 2, 1);
        Right3.setRotationPoint(-8F, 15F, -2F);
        Right3.setTextureSize(64, 64);
        Right3.mirror = true;
        setRotation(Right3, 0F, 0F, 0F);
        Right4 = new ModelRenderer(this, 0, 0);
        Right4.addBox(0F, 0F, 0F, 5, 2, 1);
        Right4.setRotationPoint(-8F, 15F, 1F);
        Right4.setTextureSize(64, 64);
        Right4.mirror = true;
        setRotation(Right4, 0F, 0F, 0F);
        TurbineCase = new ModelRenderer(this, 0, 19);
        TurbineCase.addBox(0F, 0F, 0F, 1, 4, 1);
        TurbineCase.setRotationPoint(-0.5F, 14.1F, 0F);
        TurbineCase.setTextureSize(64, 64);
        TurbineCase.mirror = true;
        setRotation(TurbineCase, 0F, 0F, 0F);
        Base = new ModelRenderer(this, 0, 33);
        Base.addBox(0F, 0F, 0F, 14, 1, 14);
        Base.setRotationPoint(-7F, 23F, -7F);
        Base.setTextureSize(64, 64);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Platform = new ModelRenderer(this, 0, 0);
        Platform.addBox(0F, 0F, 0F, 6, 1, 12);
        Platform.setRotationPoint(-3F, 18F, -6F);
        Platform.setTextureSize(64, 64);
        Platform.mirror = true;
        setRotation(Platform, 0F, 0F, 0F);
        Leg1 = new ModelRenderer(this, 0, 48);
        Leg1.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg1.setRotationPoint(-2F, 18F, 4F);
        Leg1.setTextureSize(64, 64);
        Leg1.mirror = true;
        setRotation(Leg1, 0F, 0F, 0.5759587F);
        Leg2 = new ModelRenderer(this, 0, 48);
        Leg2.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg2.setRotationPoint(-2F, 18F, -5F);
        Leg2.setTextureSize(64, 64);
        Leg2.mirror = true;
        setRotation(Leg2, 0F, 0F, 0.5759587F);
        Leg3 = new ModelRenderer(this, 0, 48);
        Leg3.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg3.setRotationPoint(1.4F, 18.7F, 4F);
        Leg3.setTextureSize(64, 64);
        Leg3.mirror = true;
        setRotation(Leg3, 0F, 0F, -0.5759587F);
        Leg4 = new ModelRenderer(this, 0, 48);
        Leg4.addBox(0F, 0F, 0F, 1, 6, 1);
        Leg4.setRotationPoint(1.4F, 18.7F, -5F);
        Leg4.setTextureSize(64, 64);
        Leg4.mirror = true;
        setRotation(Leg4, 0F, 0F, -0.5759587F);
        Top = new ModelRenderer(this, 0, 19);
        Top.addBox(0F, 0F, 0F, 6, 1, 12);
        Top.setRotationPoint(-3F, 13F, -6F);
        Top.setTextureSize(64, 64);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);
        Blade = new ModelRenderer(this, 0, 0);
        Blade.addBox(0F, 0F, 0F, 1, 4, 2);
        Blade.setRotationPoint(-0.5F, 14F, -3F);
        Blade.setTextureSize(64, 64);
        Blade.mirror = true;
        setRotation(Blade, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Left1.render(f5);
        Left2.render(f5);
        Left3.render(f5);
        Left4.render(f5);
        Right1.render(f5);
        Right2.render(f5);
        Right3.render(f5);
        Right4.render(f5);
        TurbineCase.render(f5);
        Base.render(f5);
        Platform.render(f5);
        Leg1.render(f5);
        Leg2.render(f5);
        Leg3.render(f5);
        Leg4.render(f5);
        Top.render(f5);
        Blade.render(f5);
    }

    public void renderModel(float size, float rotation){
        Left1.render(size);
        Left2.render(size);
        Left3.render(size);
        Left4.render(size);
        Right1.render(size);
        Right2.render(size);
        Right3.render(size);
        Right4.render(size);

        Base.render(size);
        Platform.render(size);
        Leg1.render(size);
        Leg2.render(size);
        Leg3.render(size);
        Leg4.render(size);

        rotation++;
        int bladeCount = 3;
        // rotation = 0;
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 3D / 16D);
        for(int i = 0; i < bladeCount; i++) {
            GL11.glPushMatrix();
            GL11.glRotated(-rotation * 2 + (i + 0.5D) / bladeCount * 360, 0, 1, 0);
            GL11.glTranslated(0, 0, 1D / 16D);
            Blade.render(size);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        GL11.glRotated(180, 0, 1, 0);

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 3D / 16D);
        for(int i = 0; i < bladeCount; i++) {
            GL11.glPushMatrix();
            GL11.glRotated(rotation * 2 + (double)i / (double)bladeCount * 360, 0, 1, 0);
            GL11.glTranslated(0, 0, 1D / 16D);
            Blade.render(size);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(0.5D, 0.5D, 0.5D, 1.0D);
        int casePoints = 20;
        GL11.glPushMatrix();
        for(int i = 0; i < casePoints; i++) {
            GL11.glPushMatrix();
            GL11.glTranslated(0, 0, 3F / 16F);
            GL11.glRotated((double)i / (double)casePoints * 275D - 130, 0, 1, 0);
            GL11.glTranslated(0, 0, 2.5F / 16F);
            TurbineCase.render(size);
            GL11.glPopMatrix();
        }
        GL11.glRotated(180, 0, 1, 0);
        for(int i = 0; i < casePoints; i++) {
            GL11.glPushMatrix();
            GL11.glTranslated(0, 0, 3F / 16F);
            GL11.glRotated((double)i / (double)casePoints * 275D - 130, 0, 1, 0);
            GL11.glTranslated(0, 0, 2.5F / 16F);
            TurbineCase.render(size);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4d(1, 1, 1, 0.3D);
        Top.render(size);
        GL11.glDisable(GL11.GL_BLEND);

        drawPlusAndMinus();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawPlusAndMinus(){
        double scale = 0.05D;
        GL11.glPushMatrix();
        GL11.glTranslated(0.26D, 13.95D / 16D, 0);
        GL11.glRotated(90, 1, 0, 0);
        GL11.glScaled(scale, scale, scale);
        GL11.glColor4d(0, 1, 0, 1);
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(-1, 0, 0);
        tess.addVertex(1, 0, 0);
        tess.addVertex(0, -1, 0);
        tess.addVertex(0, 1, 0);
        tess.draw();
        GL11.glTranslated(-0.52D / scale, 0, 0);
        GL11.glColor4d(1, 0, 0, 1);
        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(-1, 0, 0);
        tess.addVertex(1, 0, 0);
        tess.draw();
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity te){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_VACUUM_PUMP;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return true;
    }

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){
        if(te instanceof TileEntityVacuumPump) {
            GL11.glRotated(-90, 0, 1, 0);
            TileEntityVacuumPump tile = (TileEntityVacuumPump)te;
            renderModel(size, tile.oldRotation + (tile.rotation - tile.oldRotation) * partialTicks);
        } else {
            renderModel(size, 0);
        }
    }

}
