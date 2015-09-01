package pneumaticCraft.client.render.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.ClientEventHandler;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.lib.Models;

public class RenderItemPneumaticHelmet implements IItemRenderer{
    private IModelCustom helmetModel, faceModel, eyesModel;

    public static RenderItemPneumaticHelmet INSTANCE = new RenderItemPneumaticHelmet();

    public void render(EntityLivingBase entityLiving){
        GL11.glPushMatrix();
        float rot1 = entityLiving.prevRotationYawHead + (entityLiving.rotationYawHead - entityLiving.prevRotationYawHead) * ClientEventHandler.playerRenderPartialTick;
        float rot2 = entityLiving.prevRenderYawOffset + (entityLiving.renderYawOffset - entityLiving.prevRenderYawOffset) * ClientEventHandler.playerRenderPartialTick;
        GL11.glRotated(rot1 - rot2, 0, 1, 0);
        GL11.glRotated(entityLiving.prevRotationPitch + (entityLiving.rotationPitch - entityLiving.prevRotationPitch) * ClientEventHandler.playerRenderPartialTick, 1, 0, 0);
        GL11.glTranslated(-0.08, 0.1, -0.4);
        double scale = 1.5 / 16D;
        GL11.glScaled(scale, -scale, -scale);
        render();
        GL11.glPopMatrix();
    }

    public void render(){
        if(helmetModel == null) {
            helmetModel = AdvancedModelLoader.loadModel(Models.PNEUMATIC_HELMET);
            eyesModel = AdvancedModelLoader.loadModel(Models.PNEUMATIC_HELMET_EYES);
            faceModel = AdvancedModelLoader.loadModel(Models.PNEUMATIC_HELMET_FACE);
            if(!Config.useHelmetModel) {
                PneumaticCraft.proxy.getPlayer().addChatComponentMessage(new ChatComponentTranslation("message.date.ironman"));
            }
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(1, 0.2, 0.2, 1);
        helmetModel.renderAll();
        GL11.glColor4d(1, 1, 0.7, 1);
        faceModel.renderAll();
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_CULL_FACE);
        eyesModel.renderAll();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type){
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper){
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data){
        switch(type){
            case ENTITY: {
                GL11.glRotated(180, 1, 0, 0);
                render(0.0F, 3.0F, 1.0F, 0.08F);
                return;
            }
            case EQUIPPED: {
                GL11.glRotatef(180F, 1, 0.0F, 0.0F);
                GL11.glRotatef(180F, 0, 1F, 0.0F);
                //  GL11.glRotatef(70F, 0, 0F, 1.0F);
                render(-3F, 0F, 2F, 0.15F);
                return;
            }
            case EQUIPPED_FIRST_PERSON: {
                GL11.glRotatef(-150F, 1, 0F, 0.0F);
                GL11.glRotatef(90F, 0, 1F, 0.0F);
                render(0.0F, -7F, 5F, 0.2F);
                return;
            }
            case INVENTORY: {
                GL11.glRotated(180, 1, 0, 0);
                render(2.8F, 7.0F, 0.0F, 0.15F);
                return;
            }
            default:
                return;
        }
    }

    private void render(float x, float y, float z, float scale){

        GL11.glPushMatrix();
        GL11.glRotatef(-90F, 1F, 0, 0);
        // GL11.glDisable(GL11.GL_LIGHTING);
        // Scale, Translate, Rotate
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(x, y, z);
        //     GL11.glRotatef(-90F, 1F, 0, 0);
        GL11.glRotatef(-90F, 1F, 0, 0);
        // Render
        render();
        // GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
