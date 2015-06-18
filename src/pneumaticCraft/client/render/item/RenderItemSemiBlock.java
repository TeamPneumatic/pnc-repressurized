package pneumaticCraft.client.render.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.semiblock.ClientSemiBlockManager;
import pneumaticCraft.client.semiblock.ISemiBlockRenderer;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemSemiBlock implements IItemRenderer{
    private final ISemiBlock renderSemiBlock;

    public RenderItemSemiBlock(String key){
        renderSemiBlock = SemiBlockManager.getSemiBlockForKey(key);
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
                render(-0.5F, 0.0F, 0.5F, 0.5F);
                return;
            }
            case EQUIPPED: {
                render(-0.1F, 0.0F, 1.0F, 1.0F);
                return;
            }
            case EQUIPPED_FIRST_PERSON: {
                //   GL11.glRotatef(-90F, 0, 1F, 0);
                render(0.5F, 0.7F, 1.9F, 0.5F);
                return;
            }
            case INVENTORY: {
                render(0.0F, -0.08F, 1.0F, 1.0F);
                return;
            }
            default:
                return;
        }
    }

    private void render(float x, float y, float z, float scale){
        GL11.glPushMatrix();
        // GL11.glDisable(GL11.GL_LIGHTING);
        // GL11.glDisable(GL11.GL_TEXTURE_2D);
        // GL11.glColor4f(0.82F, 0.56F, 0.09F, 1.0F);
        // Scale, Translate, Rotate
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(-90F, 1F, 0, 0);

        // Bind texture

        ISemiBlockRenderer renderer = ClientSemiBlockManager.getRenderer(renderSemiBlock);
        if(renderer != null) renderer.render(renderSemiBlock, 0);

        // GL11.glEnable(GL11.GL_TEXTURE_2D);
        // GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
