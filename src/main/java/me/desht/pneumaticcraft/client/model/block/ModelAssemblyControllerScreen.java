package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import net.minecraft.client.model.ModelRenderer;

public class ModelAssemblyControllerScreen extends AbstractModelRenderer.BaseModel {
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

    public void renderModel(float scale) {
        screen.render(scale);
    }
}
