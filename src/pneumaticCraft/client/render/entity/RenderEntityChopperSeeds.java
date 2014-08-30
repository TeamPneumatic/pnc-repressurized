package pneumaticCraft.client.render.entity;

import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.entity.projectile.EntityChopperSeeds;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityChopperSeeds extends RenderEntity{

    private RenderItem itemRenderer;
    private final ItemStack iStack = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.CHOPPER_PLANT_DAMAGE);

    public RenderEntityChopperSeeds(){
        itemRenderer = new RenderItem(){
            @Override
            public boolean shouldBob(){

                return false;
            };
        };
        itemRenderer.setRenderManager(RenderManager.instance);
    }

    public void renderChopperSeeds(EntityChopperSeeds entity, double x, double y, double z, float var1, float partialTicks){
        float scaleFactor = 0.7F;
        GL11.glPushMatrix(); // start
        GL11.glTranslatef((float)x, (float)y, (float)z); // size
        // GL11.glScalef(1.0F, -1F, -1F);
        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
        EntityItem ghostEntityItem = new EntityItem(entity.worldObj);
        ghostEntityItem.hoverStart = 0.0F;
        ghostEntityItem.setEntityItemStack(iStack);
        double radius = 0.25D;
        for(int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glTranslated(Math.sin(0.5D * Math.PI * i + (entity.ticksExisted + partialTicks) * 0.4D) * radius, 0, Math.cos(0.5D * Math.PI * i + (entity.ticksExisted + partialTicks) * 0.4D) * radius);
            itemRenderer.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9){
        renderChopperSeeds((EntityChopperSeeds)par1Entity, par2, par4, par6, par8, par9);
    }

}
