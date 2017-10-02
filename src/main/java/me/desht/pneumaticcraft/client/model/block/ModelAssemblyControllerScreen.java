package me.desht.pneumaticcraft.client.model.block;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelAssemblyControllerScreen extends ModelBase {
    private final ModelRenderer screen;

    public ModelAssemblyControllerScreen() {
        textureWidth = 64;
        textureHeight = 64;

        screen = new ModelRenderer(this, 33, 32);
        screen.addBox(0F, 0F, 0F, 10, 6, 1);
        screen.setRotationPoint(-5F, 8F, 1F);
        screen.setTextureSize(64, 32);
        screen.mirror = true;
        setRotation(screen, -0.5934119F, 0F, 0F);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void renderModel(float scale) {
        screen.render(scale);
    }
}
