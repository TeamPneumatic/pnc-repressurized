package me.desht.pneumaticcraft.client.model.block;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractTileModelRenderer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.item.ItemEntity;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.MAX_PROGRESS;

public class ModelPressureChamberInterface extends AbstractTileModelRenderer.BaseModel {
    private final RendererModel input;
    private final RendererModel output;
    private ItemRenderer customRenderItem = null;

    public ModelPressureChamberInterface() {
        textureWidth = 128;
        textureHeight = 128;

        // just the doors; rest of the block is a static OBJ model
        input = new RendererModel(this, 0, 84);
        input.addBox(0F, 0F, 0F, 10, 10, 2);
        input.setRotationPoint(-5F, 11F, -7.2F);
        input.setTextureSize(128, 128);
        input.mirror = true;
        setRotation(input, 0F, 0F, 0F);
        output = new RendererModel(this, 24, 84);
        output.addBox(0F, 0F, 0F, 10, 10, 2);
        output.setRotationPoint(-5F, 11F, 5.2F);
        output.setTextureSize(128, 128);
        output.mirror = true;
        setRotation(output, 0F, 0F, 0F);
    }

    private void renderDoors(float size, float inputDoor, float outputDoor){
        GlStateManager.pushMatrix();
        GlStateManager.translated((1F - (float)Math.cos(inputDoor * Math.PI)) * 0.37F, 0, 0);
        input.render(size);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translated((1F - (float)Math.cos(outputDoor * Math.PI)) * 0.37F, 0, 0);
        output.render(size);
        GlStateManager.popMatrix();
    }

    public void renderModel(float size, TileEntityPressureChamberInterface te, float partialTicks, ItemEntity ghostEntityItem) {
        float renderInputProgress = te.oldInputProgress + (te.inputProgress - te.oldInputProgress) * partialTicks;
        float renderOutputProgress = te.oldOutputProgress + (te.outputProgress - te.oldOutputProgress) * partialTicks;
        renderDoors(size, renderInputProgress / MAX_PROGRESS, renderOutputProgress / MAX_PROGRESS);

        if (ghostEntityItem != null) {
            if (customRenderItem == null) {
                customRenderItem = new AbstractTileModelRenderer.NoBobItemRenderer();
            }

            float zOff = 0f;
            if (te.interfaceMode == TileEntityPressureChamberInterface.InterfaceDirection.IMPORT && renderOutputProgress >= MAX_PROGRESS - 5) {
                // render item moving out of the interface into the chamber (always in +Z direction due to matrix rotation)
                zOff = (1.0f - (MAX_PROGRESS - renderOutputProgress) + partialTicks) / 3f;
            }

            GlStateManager.translated(0, 1.25f, zOff);
            GlStateManager.scaled(1.0F, -1F, -1F);

            EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
            renderManager.options.fancyGraphics = fancySetting;
        }
    }
}
