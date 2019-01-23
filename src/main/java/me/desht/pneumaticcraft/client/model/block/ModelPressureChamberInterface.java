package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.MAX_PROGRESS;

public class ModelPressureChamberInterface extends AbstractModelRenderer.BaseModel {
    private final ModelRenderer input;
    private final ModelRenderer output;
    private RenderEntityItem customRenderItem = null;

    public ModelPressureChamberInterface() {
        textureWidth = 128;
        textureHeight = 128;

        // just the doors; rest of the block is a static OBJ model
        input = new ModelRenderer(this, 0, 84);
        input.addBox(0F, 0F, 0F, 10, 10, 2);
        input.setRotationPoint(-5F, 11F, -7.2F);
        input.setTextureSize(128, 128);
        input.mirror = true;
        setRotation(input, 0F, 0F, 0F);
        output = new ModelRenderer(this, 24, 84);
        output.addBox(0F, 0F, 0F, 10, 10, 2);
        output.setRotationPoint(-5F, 11F, 5.2F);
        output.setTextureSize(128, 128);
        output.mirror = true;
        setRotation(output, 0F, 0F, 0F);
    }

    private void renderDoors(float size, float inputDoor, float outputDoor){
        GlStateManager.pushMatrix();
        GlStateManager.translate((1F - (float)Math.cos(inputDoor * Math.PI)) * 0.37F, 0, 0);
        input.render(size);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate((1F - (float)Math.cos(outputDoor * Math.PI)) * 0.37F, 0, 0);
        output.render(size);
        GlStateManager.popMatrix();
    }

    public void renderModel(float size, TileEntityPressureChamberInterface te, float partialTicks, EntityItem ghostEntityItem) {
        float renderInputProgress = te.oldInputProgress + (te.inputProgress - te.oldInputProgress) * partialTicks;
        float renderOutputProgress = te.oldOutputProgress + (te.outputProgress - te.oldOutputProgress) * partialTicks;
        renderDoors(size, renderInputProgress / MAX_PROGRESS, renderOutputProgress / MAX_PROGRESS);

        if (ghostEntityItem != null) {
            if (customRenderItem == null) {
                customRenderItem = new AbstractModelRenderer.NoBobItemRenderer();
            }

            float zOff = 0f;
            if (te.interfaceMode == TileEntityPressureChamberInterface.EnumInterfaceMode.IMPORT && renderOutputProgress >= MAX_PROGRESS - 5) {
                // render item moving out of the interface into the chamber (always in +Z direction due to matrix rotation)
                zOff = (1.0f - (MAX_PROGRESS - renderOutputProgress) + partialTicks) / 3f;
            }

            GlStateManager.translate(0, 1.25f, zOff);
            GlStateManager.scale(1.0F, -1F, -1F);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
            renderManager.options.fancyGraphics = fancySetting;
        }
    }
}
