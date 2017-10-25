package me.desht.pneumaticcraft.client.model.block;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class ModelVacuumPump extends ModelBase {
    ModelRenderer TurbineCase;
    ModelRenderer Top;
    ModelRenderer Blade;

    public ModelVacuumPump() {
        textureWidth = 64;
        textureHeight = 64;

        TurbineCase = new ModelRenderer(this, 0, 47);
        TurbineCase.addBox(0F, 0F, 0F, 1, 4, 1);
        TurbineCase.setRotationPoint(-0.5F, 14.1F, 0F);
        TurbineCase.setTextureSize(64, 64);
        TurbineCase.mirror = true;
        setRotation(TurbineCase, 0F, 0F, 0F);
        Top = new ModelRenderer(this, 0, 47);
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

    private static final int BLADE_COUNT = 3;
    private static final int CASE_POINTS = 20;

    public void renderModel(float size, float rotation) {
        rotation++;
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 3D / 16D);
        for (int i = 0; i < BLADE_COUNT; i++) {
            GL11.glPushMatrix();
            GL11.glRotated(-rotation * 2 + (i + 0.5D) / BLADE_COUNT * 360, 0, 1, 0);
            GL11.glTranslated(0, 0, 1D / 16D);
            Blade.render(size);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        GL11.glRotated(180, 0, 1, 0);

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 3D / 16D);
        for (int i = 0; i < BLADE_COUNT; i++) {
            GL11.glPushMatrix();
            GL11.glRotated(rotation * 2 + (double) i / (double) BLADE_COUNT * 360, 0, 1, 0);
            GL11.glTranslated(0, 0, 1D / 16D);
            Blade.render(size);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(0.75D, 0.75D, 0.75D, 1.0D);
        GL11.glPushMatrix();
        for (int i = 0; i < CASE_POINTS; i++) {
            GL11.glPushMatrix();
            GL11.glTranslated(0, 0, 3F / 16F);
            GL11.glRotated((double) i / (double) CASE_POINTS * 275D - 130, 0, 1, 0);
            GL11.glTranslated(0, 0, 2.5F / 16F);
            TurbineCase.render(size);
            GL11.glPopMatrix();
        }
        GL11.glRotated(180, 0, 1, 0);
        for (int i = 0; i < CASE_POINTS; i++) {
            GL11.glPushMatrix();
            GL11.glTranslated(0, 0, 3F / 16F);
            GL11.glRotated((double) i / (double) CASE_POINTS * 275D - 130, 0, 1, 0);
            GL11.glTranslated(0, 0, 2.5F / 16F);
            TurbineCase.render(size);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4d(1, 1, 1, 0.4D);
        Top.render(size);
        GL11.glDisable(GL11.GL_BLEND);

        drawPlusAndMinus();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawPlusAndMinus() {
        double scale = 0.05D;
        GL11.glPushMatrix();
        GL11.glTranslated(0.26D, 13.95D / 16D, 0);
        GL11.glRotated(90, 1, 0, 0);
        GL11.glScaled(scale, scale, scale);
        GL11.glColor4d(0, 1, 0, 1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(-1, 0, 0).endVertex();
        bufferBuilder.pos(1, 0, 0).endVertex();
        bufferBuilder.pos(0, -1, 0).endVertex();
        bufferBuilder.pos(0, 1, 0).endVertex();
        tessellator.draw();
        GL11.glTranslated(-0.52D / scale, 0, 0);
        GL11.glColor4d(1, 0, 0, 1);
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(-1, 0, 0).endVertex();
        bufferBuilder.pos(1, 0, 0).endVertex();
        tessellator.draw();
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
