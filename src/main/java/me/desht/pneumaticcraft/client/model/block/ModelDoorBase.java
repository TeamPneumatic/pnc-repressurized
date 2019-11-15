package me.desht.pneumaticcraft.client.model.block;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractTileModelRenderer;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelDoorBase extends AbstractTileModelRenderer.BaseModel {
    private final RendererModel cylinder1;
    private final RendererModel cylinder2;
    private final RendererModel cylinder3;

    public ModelDoorBase() {
        textureWidth = 64;
        textureHeight = 64;

        cylinder1 = new RendererModel(this, 0, 28);
        cylinder1.addBox(0F, 0F, 0F, 3, 3, 10);
        cylinder1.setRotationPoint(2.5F, 8.5F, -6F);
        cylinder1.setTextureSize(64, 32);
        cylinder1.mirror = true;
        setRotation(cylinder1, 0F, 0F, 0F);
        cylinder2 = new RendererModel(this, 0, 28);
        cylinder2.addBox(0F, 0F, 0F, 2, 2, 10);
        cylinder2.setRotationPoint(3F, 9F, -6F);
        cylinder2.setTextureSize(64, 32);
        cylinder2.mirror = true;
        setRotation(cylinder2, 0F, 0F, 0F);
        cylinder3 = new RendererModel(this, 0, 28);
        cylinder3.addBox(0F, 0F, 0F, 1, 1, 10);
        cylinder3.setRotationPoint(3.5F, 9.5F, -6F);
        cylinder3.setTextureSize(64, 32);
        cylinder3.mirror = true;
        setRotation(cylinder3, 0F, 0F, 0F);
    }

    public void renderModel(float size, float progress, boolean rightGoing) {
        float cosinus = /*12 / 16F -*/(float) Math.sin(Math.toRadians((1 - progress) * 90)) * 12 / 16F;
        float sinus = 9 / 16F - (float) Math.cos(Math.toRadians((1 - progress) * 90)) * 9 / 16F;
        double extension = Math.sqrt(Math.pow(sinus, 2) + Math.pow(cosinus + 4 / 16F, 2));
        GlStateManager.translated(((rightGoing ? -4 : 0) + 2.5) / 16F, 0, -6 / 16F);
        double cylinderAngle = Math.toDegrees(Math.atan(sinus / (cosinus + 14 / 16F)));
        GlStateManager.rotated((float) cylinderAngle, 0f, rightGoing ? 1f : -1f, 0f);
        GlStateManager.translated(((rightGoing ? -3 : 0) - 2.5) / 16F, 0, 6 / 16F);
        double extensionPart = extension * 0.5D;
        cylinder1.render(size);
        GlStateManager.translated(0, 0, extensionPart);
        cylinder2.render(size);
        GlStateManager.translated(0, 0, extensionPart);
        cylinder3.render(size);
    }
}
