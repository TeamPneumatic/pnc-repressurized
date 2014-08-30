package pneumaticCraft.client.render.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.ModelAirCannon;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemCannonParts implements IItemRenderer{

    private final ModelAirCannon model;
    private final boolean cannonBase;

    public RenderItemCannonParts(boolean cannonBase){
        model = new ModelAirCannon();
        this.cannonBase = cannonBase;
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
                render(0.0F, 0.0F, 1.0F, 1.0F);
                return;
            }
            case EQUIPPED: {
                render(0.2F, -0.2F, 1.3F, 2.0F);
                return;
            }
            case EQUIPPED_FIRST_PERSON: {
                render(0.7F, -0.0F, item.getItem() == Itemss.cannonBarrel ? 1.0F : 1.2F, 2.0F);
                return;
            }
            case INVENTORY: {
                render(0.1F, item.getItem() == Itemss.cannonBarrel ? -0.05F : 0F, item.getItem() == Itemss.cannonBarrel ? 1.0F : 1.2F, 2.0F);
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
        GL11.glRotatef(-90F, 1F, 0, 0);

        // Bind texture
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_AIR_CANNON);

        // Render
        model.renderModel(1F / 16F, 0F, 00F, !cannonBase, cannonBase);

        // GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
