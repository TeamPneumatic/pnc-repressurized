package me.desht.pneumaticcraft.client.model.block;

import me.desht.pneumaticcraft.client.model.entity.ModelDroneMinigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;

public class ModelSentryTurret extends ModelBase {
    private final ModelRenderer tripot1;
    private final ModelRenderer tripot2;
    private final ModelRenderer tripot3;
    private final  ModelRenderer main;
    private final ModelRenderer main2;
    private final ModelDroneMinigun model = new ModelDroneMinigun();
    private final TileEntitySentryTurret fakeTurret = new TileEntitySentryTurret();

    public ModelSentryTurret(){
        textureWidth = 64;
        textureHeight = 32;

        tripot1 = new ModelRenderer(this, 0, 0);
        tripot1.addBox(0F, 0F, 0F, 1, 7, 1);
        tripot1.setRotationPoint(-0.5F, 18F, -1F);
        tripot1.setTextureSize(64, 32);
        tripot1.mirror = true;
        setRotation(tripot1, -0.6981317F, 0F, 0F);
        tripot2 = new ModelRenderer(this, 4, 0);
        tripot2.addBox(0F, 0F, 0F, 1, 7, 1);
        tripot2.setRotationPoint(1F, 18F, 0F);
        tripot2.setTextureSize(64, 32);
        tripot2.mirror = true;
        setRotation(tripot2, -0.6981317F, -2.094395F, 0F);
        tripot3 = new ModelRenderer(this, 8, 0);
        tripot3.addBox(0F, 0F, 0F, 1, 7, 1);
        tripot3.setRotationPoint(-0.5F, 18F, 1F);
        tripot3.setTextureSize(64, 32);
        tripot3.mirror = true;
        setRotation(tripot3, -0.6981317F, 2.094395F, 0F);
        main = new ModelRenderer(this, 12, 0);
        main.addBox(0F, 0F, 0F, 2, 1, 2);
        main.setRotationPoint(-1F, 17.5F, -1F);
        main.setTextureSize(64, 32);
        main.mirror = true;
        setRotation(main, 0F, 0F, 0F);
        main2 = new ModelRenderer(this, 12, 3);
        main2.addBox(0F, 0F, 0F, 1, 2, 1);
        main2.setRotationPoint(-0.5F, 16F, -0.5F);
        main2.setTextureSize(64, 32);
        main2.mirror = true;
        setRotation(main2, 0F, 0F, 0F);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void renderModel(float scale, TileEntitySentryTurret te, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -13 / 16D, 0);
        if(te == null) {
            model.renderMinigun(fakeTurret.getMinigun(), 1 / 16F, partialTicks, false);
        } else {
            model.renderMinigun(te.getMinigun(), 1 / 16F, partialTicks, false);
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F, -1, -1F);
            GlStateManager.translate(0, -1.45F, 0);
            BlockPos pos = te.getPos();
            te.getMinigun().render(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(Textures.MODEL_SENTRY_TURRET);
    }
}
