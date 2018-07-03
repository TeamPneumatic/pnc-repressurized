package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class ModelUniversalSensor extends AbstractModelRenderer.BaseModel {
    private final ModelRenderer dish1;
    private final ModelRenderer dish2;
    private final ModelRenderer dish3;
    private final ModelRenderer dish4;
    private final ModelRenderer dish5;
    private final ModelRenderer dish6;

    public ModelUniversalSensor(){
        textureWidth = 64;
        textureHeight = 64;

        dish1 = new ModelRenderer(this, 0, 33);
        dish1.addBox(-2F, 0F, -2F, 4, 1, 4);
        dish1.setRotationPoint(0F, 16F, 0F);
        dish1.setTextureSize(64, 64);
        dish1.mirror = true;
        setRotation(dish1, 0F, 0F, 0F);
        dish2 = new ModelRenderer(this, 0, 38);
        dish2.addBox(-3F, -1F, 0F, 1, 8, 4);
        dish2.setRotationPoint(0F, 9F, -2F);
        dish2.setTextureSize(64, 64);
        dish2.mirror = true;
        setRotation(dish2, 0F, 0F, -0.2268928F);
        dish3 = new ModelRenderer(this, 0, 50);
        dish3.addBox(-3.8F, 0F, 0.8F, 1, 4, 4);
        dish3.setRotationPoint(0F, 8F, 0F);
        dish3.setTextureSize(64, 64);
        dish3.mirror = true;
        setRotation(dish3, 0.0698132F, 0.3839724F, -0.2268928F);
        dish4 = new ModelRenderer(this, 10, 50);
        dish4.addBox(-3.8F, 0F, -4.7F, 1, 4, 4);
        dish4.setRotationPoint(0F, 8F, 0F);
        dish4.setTextureSize(64, 64);
        dish4.mirror = true;
        setRotation(dish4, -0.0698132F, -0.3839724F, -0.2268928F);
        dish5 = new ModelRenderer(this, 0, 58);
        dish5.addBox(-2F, 0F, -0.5F, 6, 1, 1);
        dish5.setRotationPoint(0F, 12F, 0F);
        dish5.setTextureSize(64, 64);
        dish5.mirror = true;
        setRotation(dish5, 0F, 0F, -0.2268928F);
        dish6 = new ModelRenderer(this, 0, 60);
        dish6.addBox(3F, 0F, -1F, 1, 1, 2);
        dish6.setRotationPoint(0F, 10.2F, 0F);
        dish6.setTextureSize(64, 64);
        dish6.mirror = true;
        setRotation(dish6, 0F, 0F, 0F);
    }

    public void renderModel(float scale, float dishRotation) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(dishRotation, 0, 1, 0);
        dish1.render(scale);
        dish2.render(scale);
        dish3.render(scale);
        dish4.render(scale);
        dish5.render(scale);
        dish6.render(scale);
        GlStateManager.popMatrix();
    }
}
