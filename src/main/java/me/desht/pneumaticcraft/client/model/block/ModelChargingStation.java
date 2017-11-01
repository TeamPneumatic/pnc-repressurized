package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ModelChargingStation extends AbstractModelRenderer.BaseModel {
    private final ModelRenderer shape3;
    private final ModelRenderer shape4;
    private final ModelRenderer shape5;
    private final ModelRenderer shape6;
    private final ModelRenderer shape7;
    private final ModelRenderer shape8;
    private final ModelRenderer shape9;
    private final ModelRenderer shape10;
    private final ModelRenderer shape11;
    private final ModelRenderer shape12;
    private final ModelRenderer shape13;
    private final ModelRenderer shape14;
    private final ModelRenderer shape15;
    private final ModelRenderer shape16;
    private RenderEntityItem customRenderItem = null;

    public ModelChargingStation() {
        textureWidth = 64;
        textureHeight = 32;

        shape3 = new ModelRenderer(this, 0, 0);
        shape3.addBox(0F, 0F, 0F, 1, 2, 1);
        shape3.setRotationPoint(-5F, 17F, -4F);
        shape3.setTextureSize(64, 32);
        shape3.mirror = true;
        setRotation(shape3, 0F, 0F, 0F);
        shape4 = new ModelRenderer(this, 0, 3);
        shape4.addBox(0F, 0F, 0F, 1, 2, 1);
        shape4.setRotationPoint(-4F, 17F, -5F);
        shape4.setTextureSize(64, 32);
        shape4.mirror = true;
        setRotation(shape4, 0F, 0F, 0F);
        shape5 = new ModelRenderer(this, 0, 9);
        shape5.addBox(0F, 0F, 0F, 1, 2, 1);
        shape5.setRotationPoint(-5F, 17F, 3F);
        shape5.setTextureSize(64, 32);
        shape5.mirror = true;
        setRotation(shape5, 0F, 0F, 0F);
        shape6 = new ModelRenderer(this, 0, 12);
        shape6.addBox(0F, 0F, 0F, 1, 2, 1);
        shape6.setRotationPoint(-4F, 17F, 4F);
        shape6.setTextureSize(64, 32);
        shape6.mirror = true;
        setRotation(shape6, 0F, 0F, 0F);
        shape7 = new ModelRenderer(this, 0, 15);
        shape7.addBox(0F, 0F, 0F, 1, 2, 1);
        shape7.setRotationPoint(3F, 17F, 4F);
        shape7.setTextureSize(64, 32);
        shape7.mirror = true;
        setRotation(shape7, 0F, 0F, 0F);
        shape8 = new ModelRenderer(this, 0, 18);
        shape8.addBox(0F, 0F, 0F, 1, 2, 1);
        shape8.setRotationPoint(4F, 17F, 3F);
        shape8.setTextureSize(64, 32);
        shape8.mirror = true;
        setRotation(shape8, 0F, 0F, 0F);
        shape9 = new ModelRenderer(this, 0, 21);
        shape9.addBox(0F, 0F, 0F, 1, 2, 1);
        shape9.setRotationPoint(4F, 17F, -4F);
        shape9.setTextureSize(64, 32);
        shape9.mirror = true;
        setRotation(shape9, 0F, 0F, 0F);
        shape10 = new ModelRenderer(this, 0, 24);
        shape10.addBox(0F, 0F, 0F, 1, 2, 1);
        shape10.setRotationPoint(3F, 17F, -5F);
        shape10.setTextureSize(64, 32);
        shape10.mirror = true;
        setRotation(shape10, 0F, 0F, 0F);
        shape11 = new ModelRenderer(this, 5, 0);
        shape11.addBox(0F, 0F, 0F, 1, 1, 8);
        shape11.setRotationPoint(-4F, 17F, -4F);
        shape11.setTextureSize(64, 32);
        shape11.mirror = true;
        setRotation(shape11, 0F, 0F, 0F);
        shape12 = new ModelRenderer(this, 5, 9);
        shape12.addBox(0F, 0F, 0F, 1, 1, 8);
        shape12.setRotationPoint(3F, 17F, -4F);
        shape12.setTextureSize(64, 32);
        shape12.mirror = true;
        setRotation(shape12, 0F, 0F, 0F);
        shape13 = new ModelRenderer(this, 23, 0);
        shape13.addBox(0F, 0F, 0F, 1, 8, 1);
        shape13.setRotationPoint(-4F, 9F, -4F);
        shape13.setTextureSize(64, 32);
        shape13.mirror = true;
        setRotation(shape13, 0F, 0F, 0F);
        shape14 = new ModelRenderer(this, 23, 9);
        shape14.addBox(0F, 0F, 0F, 1, 8, 1);
        shape14.setRotationPoint(-4F, 9F, 3F);
        shape14.setTextureSize(64, 32);
        shape14.mirror = true;
        setRotation(shape14, 0F, 0F, 0F);
        shape15 = new ModelRenderer(this, 27, 9);
        shape15.addBox(0F, 0F, 0F, 1, 8, 1);
        shape15.setRotationPoint(3F, 9F, 3F);
        shape15.setTextureSize(64, 32);
        shape15.mirror = true;
        setRotation(shape15, 0F, 0F, 0F);
        shape16 = new ModelRenderer(this, 27, 0);
        shape16.addBox(0F, 0F, 0F, 1, 8, 1);
        shape16.setRotationPoint(3F, 9F, -4F);
        shape16.setTextureSize(64, 32);
        shape16.mirror = true;
        setRotation(shape16, 0F, 0F, 0F);
    }

    public void renderModel(float scale, boolean renderChargePad, EntityItem ghostEntityItem) {
        if (renderChargePad) {
            shape3.render(scale);
            shape4.render(scale);
            shape5.render(scale);
            shape6.render(scale);
            shape7.render(scale);
            shape8.render(scale);
            shape9.render(scale);
            shape10.render(scale);
            shape11.render(scale);
            shape12.render(scale);
            shape13.render(scale);
            shape14.render(scale);
            shape15.render(scale);
            shape16.render(scale);
        }
        if (ghostEntityItem != null) {
            if (customRenderItem == null) {
                customRenderItem = new AbstractModelRenderer.NoBobItemRenderer();
            }
            GlStateManager.translate(0, 1.25f, 0);
            GlStateManager.scale(1.0F, -1F, -1F);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
            renderManager.options.fancyGraphics = fancySetting;
        }
    }
}
