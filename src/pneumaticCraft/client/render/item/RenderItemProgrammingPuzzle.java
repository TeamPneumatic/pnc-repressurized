package pneumaticCraft.client.render.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.progwidgets.IProgWidget;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemProgrammingPuzzle implements IItemRenderer{

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type){

        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper){
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data){
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        switch(type){
            case ENTITY: {
                GL11.glRotated(180, 0, 0, 1);
                GL11.glDisable(GL11.GL_CULL_FACE);
                render(0.0F, 0.0F, 0.0F, 0.5F, item);
                GL11.glEnable(GL11.GL_CULL_FACE);
                break;
            }
            case EQUIPPED: {
                GL11.glRotated(180, 0, 0, 1);
                render(-0.4F, -0.5F, -0.0F, 1.0F, item);
                //   GL11.glRotated(80, 1, 0, 0);
                break;
            }
            case EQUIPPED_FIRST_PERSON: {
                GL11.glRotated(180, 0, 0, 1);
                render(-0.8F, -0.5F, 0.3F, 1.0F, item);
                break;
            }
            case INVENTORY: {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                render(8.0F, 8.0F, 0, 12F, item);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                break;
            }
            default:
                break;
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void render(float x, float y, float z, float scale, ItemStack stack){
        IProgWidget widget = ItemProgrammingPuzzle.getWidgetForPiece(stack);
        if(widget == null) return;
        int width = widget.getWidth() + (widget.getParameters() != null && widget.getParameters().length > 0 ? 10 : 0);
        int height = widget.getHeight() + (widget.hasStepOutput() ? 5 : 0);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        // GL11.glDisable(GL11.GL_LIGHTING);
        //      GL11.glRotatef(-90F, 1F, 0, 0);
        // Scale, Translate, Rotate
        scale = scale / Math.max(height, width);
        GL11.glScalef(scale, scale, 1);
        GL11.glTranslatef(-width / 2, -height / 2, 0);
        //  GL11.glTranslatef(x, y, z);
        //GL11.glRotatef(-90F, 1F, 0, 0);

        widget.render();

        // GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
