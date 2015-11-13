package pneumaticCraft.client.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.model.entity.ModelDroneMinigun;
import pneumaticCraft.client.render.RenderProgressingLine;
import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.common.item.ItemMinigun;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.minigun.Minigun;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemMinigun implements IItemRenderer{

    private final ModelDroneMinigun model;
    private final RenderProgressingLine minigunFire = new RenderProgressingLine().setProgress(1);

    public RenderItemMinigun(){
        model = new ModelDroneMinigun();
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
        GL11.glTranslated(0, 0.7, 0);

        switch(type){
            case ENTITY: {
                render(0.0F, 0.0F, 1.0F, 1.0F, item);
                return;
            }
            case EQUIPPED: {
                GL11.glRotatef(240F, 0, 1F, 0);
                render(0.1F, 0.6F, 1.3F, 2.0F, item);
                return;
            }
            case EQUIPPED_FIRST_PERSON: {
                GL11.glRotatef(150F, 0, 1F, 0);
                GL11.glRotated(-10, 1, 0, 0);
                ItemStack heldStack = PneumaticCraft.proxy.getPlayer().getCurrentEquippedItem();
                if(heldStack != null && heldStack.getItem() == Itemss.minigun) {
                    item = heldStack;
                }
                render(-0.5F, 0.6F, 2.1F, 1.0F, item);
                renderEffects(item);
                return;
            }
            case INVENTORY: {
                render(0.0F, 0.0F, 1.0F, 1.0F, item);
                return;
            }
            default:
                return;
        }
    }

    private void renderEffects(ItemStack stack){
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        Minigun minigun = ((ItemMinigun)Itemss.minigun).getMinigun(stack, player);
        if(minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            RenderUtils.glColorHex(0xFF000000 | minigun.getAmmoColor());
            for(int i = 0; i < 5; i++) {
                minigunFire.startX = -0.5;
                minigunFire.startY = 0.6;
                minigunFire.startZ = 0.4;
                minigunFire.endX = 0.2 * (player.getRNG().nextDouble() - 0.5) - 1;
                minigunFire.endY = 0.2 * (player.getRNG().nextDouble() - 0.5);
                minigunFire.endZ = 7;
                minigunFire.render();
            }
            GL11.glColor4d(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    private void render(float x, float y, float z, float scale, ItemStack stack){
        GL11.glPushMatrix();
        GL11.glRotatef(-90F, 1F, 0, 0);
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(-90F, 1F, 0, 0);
        Minigun minigun = ((ItemMinigun)stack.getItem()).getMinigun(stack, PneumaticCraft.proxy.getPlayer());
        model.renderMinigun(minigun, 1 / 16F, 0, false);
        minigun.render(0, 0, 0, 0.6D);
        GL11.glPopMatrix();
    }

}
