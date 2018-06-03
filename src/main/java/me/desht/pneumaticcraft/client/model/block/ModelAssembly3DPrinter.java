package me.desht.pneumaticcraft.client.model.block;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Assembly 3D Printer - MineMaarten
 * Created using Tabula 7.0.0
 */
public class ModelAssembly3DPrinter extends ModelBase {
    public ModelRenderer support1;
    public ModelRenderer support2;
    public ModelRenderer support3;
    public ModelRenderer support4;
    public ModelRenderer supportHor1;
    public ModelRenderer supportHor2;
    public ModelRenderer railY1;
    public ModelRenderer railY2;
    public ModelRenderer railX;
    public ModelRenderer printerHead;
    public ModelRenderer printerNozzle;

    public ModelAssembly3DPrinter() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.railY1 = new ModelRenderer(this, 16, 4);
        this.railY1.setRotationPoint(-8.0F, 8.0F, -7.0F);
        this.railY1.addBox(0.0F, 0.0F, 0.0F, 16, 1, 1, 0.0F);
        this.support4 = new ModelRenderer(this, 12, 0);
        this.support4.setRotationPoint(-8.0F, 8.0F, 7.0F);
        this.support4.addBox(0.0F, 0.0F, 0.0F, 1, 16, 1, 0.0F);
        this.railX = new ModelRenderer(this, 4, 8);
        this.railX.setRotationPoint(0.0F, 0.0F, 1.0F);
        this.railX.addBox(0.0F, 0.0F, 0.0F, 4, 1, 12, 0.0F);
        this.support3 = new ModelRenderer(this, 8, 0);
        this.support3.setRotationPoint(-8.0F, 8.0F, -8.0F);
        this.support3.addBox(0.0F, 0.0F, 0.0F, 1, 16, 1, 0.0F);
        this.support1 = new ModelRenderer(this, 0, 0);
        this.support1.setRotationPoint(7.0F, 8.0F, 7.0F);
        this.support1.addBox(0.0F, 0.0F, 0.0F, 1, 16, 1, 0.0F);
        this.supportHor1 = new ModelRenderer(this, 16, 0);
        this.supportHor1.setRotationPoint(-7.0F, 8.0F, 7.0F);
        this.supportHor1.addBox(0.0F, 0.0F, 0.0F, 14, 1, 1, 0.0F);
        this.printerHead = new ModelRenderer(this, 49, 0);
        this.printerHead.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.printerHead.addBox(0.0F, 0.0F, 0.0F, 3, 2, 3, 0.0F);
        this.printerNozzle = new ModelRenderer(this, 46, 0);
        this.printerNozzle.setRotationPoint(1.0F, 2.0F, 1.0F);
        this.printerNozzle.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.supportHor2 = new ModelRenderer(this, 16, 2);
        this.supportHor2.setRotationPoint(-7.0F, 8.0F, -8.0F);
        this.supportHor2.addBox(0.0F, 0.0F, 0.0F, 14, 1, 1, 0.0F);
        this.railY2 = new ModelRenderer(this, 16, 6);
        this.railY2.setRotationPoint(0.0F, 0.0F, 13.0F);
        this.railY2.addBox(0.0F, 0.0F, 0.0F, 16, 1, 1, 0.0F);
        this.support2 = new ModelRenderer(this, 4, 0);
        this.support2.setRotationPoint(7.0F, 8.0F, -8.0F);
        this.support2.addBox(0.0F, 0.0F, 0.0F, 1, 16, 1, 0.0F);
        this.railY1.addChild(this.railX);
        this.railX.addChild(this.printerHead);
        this.printerHead.addChild(this.printerNozzle);
        this.railY1.addChild(this.railY2);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.railY1.render(f5);
        this.support4.render(f5);
        this.support3.render(f5);
        this.support1.render(f5);
        this.supportHor1.render(f5);
        this.supportHor2.render(f5);
        this.support2.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
