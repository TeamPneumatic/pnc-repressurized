package pneumaticCraft.client.render.itemblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.ModelPressureTube;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemPressureTube implements IItemRenderer{

    private final ModelPressureTube model;
    private final boolean advanced;

    public RenderItemPressureTube(boolean advanced){
        model = new ModelPressureTube();
        this.advanced = advanced;
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
        int itemDamage = item.getItemDamage();
        switch(type){
            case ENTITY: {
                render(0.0F, 0.0F, 1.0F, 1.0F, itemDamage);
                return;
            }
            case EQUIPPED: {
                GL11.glRotated(180, 0, 1, 0);
                // GL11.glTranslated(0, 2, 0);
                render(-0.7F, 0.4F, 0.3F, 0.8F, itemDamage);
                return;
            }
            case EQUIPPED_FIRST_PERSON: {
                GL11.glRotated(180, 0, 1, 0);
                render(0.0F, 1F, 0.4F, 1.2F, itemDamage);
                return;
            }
            case INVENTORY: {
                render(0.0F, 0.0F, 1.0F, 1.5F, itemDamage);
                return;
            }
            default:
                return;
        }
    }

    private void render(float x, float y, float z, float scale, int itemDamage){
        GL11.glPushMatrix();
        // GL11.glDisable(GL11.GL_LIGHTING);
        // GL11.glDisable(GL11.GL_TEXTURE_2D);
        // GL11.glColor4f(0.82F, 0.56F, 0.09F, 1.0F);
        // Scale, Translate, Rotate
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(-90F, 1F, 0, 0);

        // Bind texture

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(advanced ? Textures.MODEL_ADVANCED_PRESSURE_TUBE : Textures.MODEL_PRESSURE_TUBE);

        // Render
        model.renderModel(1F / 16F, new boolean[]{true, true, false, false, false, false});

        // GL11.glEnable(GL11.GL_TEXTURE_2D);
        // GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
