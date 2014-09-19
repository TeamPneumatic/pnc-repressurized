package pneumaticCraft.client.render.itemblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.block.tubes.TubeModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemTubeModule implements IItemRenderer{

    private final TubeModule module;

    public RenderItemTubeModule(TubeModule module){
        this.module = module;
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
                render(-0.6F, -0.8F, -0.5F, 1.0F, itemDamage);
                return;
            }
            case EQUIPPED: {
                GL11.glRotated(180, 0, 1, 0);
                // GL11.glTranslated(0, 2, 0);
                render(-0.8F, -0.4F, -0.8F, 2F, itemDamage);
                return;
            }
            case EQUIPPED_FIRST_PERSON: {
                GL11.glRotated(180, 0, 1, 0);
                render(-0.2F, 0.2F, -1.0F, 1.5F, itemDamage);
                return;
            }
            case INVENTORY: {
                render(0.0F, -0.4F, 0.0F, 3F, itemDamage);
                return;
            }
            default:
                return;
        }
    }

    private void render(float x, float y, float z, float scale, int itemDamage){
        module.setDirection(ForgeDirection.UP);
        GL11.glPushMatrix();
        scale *= 0.2 * (1 / module.getWidth());
        GL11.glScalef(scale, scale, scale);
        GL11.glDisable(GL11.GL_CULL_FACE);
        module.renderDynamic(x, y, z, 0, 0, true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }
}
