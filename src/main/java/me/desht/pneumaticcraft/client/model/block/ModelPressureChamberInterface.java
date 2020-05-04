package me.desht.pneumaticcraft.client.model.block;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractTileModelRenderer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.MathHelper;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.MAX_PROGRESS;

public class ModelPressureChamberInterface extends AbstractTileModelRenderer.BaseModel {
    private final RendererModel input;
    private final RendererModel output;

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
        if (inputDoor < 1f) {
            GlStateManager.pushMatrix();
            GlStateManager.translated((1F - (float) Math.cos(inputDoor * Math.PI)) * 0.37F, 0, 0);
            GlStateManager.scaled(1f - inputDoor, 1, 1);
            input.render(size);
            GlStateManager.popMatrix();
        }
        if (outputDoor < 1f) {
            GlStateManager.pushMatrix();
            GlStateManager.translated((1F - (float) Math.cos(outputDoor * Math.PI)) * 0.37F, 0, 0);
            GlStateManager.scaled(1f - outputDoor, 1, 1);
            output.render(size);
            GlStateManager.popMatrix();
        }
    }

    public void renderModel(float size, TileEntityPressureChamberInterface te, float partialTicks) {
        float renderInputProgress = MathHelper.lerp(partialTicks, te.oldInputProgress, te.inputProgress);
        float renderOutputProgress = MathHelper.lerp(partialTicks, te.oldOutputProgress, te.outputProgress);
        renderDoors(size, renderInputProgress / MAX_PROGRESS, renderOutputProgress / MAX_PROGRESS);
    }
}
