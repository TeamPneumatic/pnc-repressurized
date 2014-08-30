package pneumaticCraft.client.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.common.PneumaticCraftUtils;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderModelBase extends TileEntitySpecialRenderer implements IItemRenderer{

    private final IBaseModel model;

    public RenderModelBase(IBaseModel model){
        this.model = model;
    }

    /*
     * TileEntitySpecialRenderer part
     */

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f){
        renderModelAt(tileentity, d0, d1, d2, f);
    }

    public void renderModelAt(TileEntity tile, double d, double d1, double d2, float f){
        GL11.glPushMatrix();
        if(model.getModelTexture() != null) FMLClientHandler.instance().getClient().getTextureManager().bindTexture(model.getModelTexture());
        GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        if(model.rotateModelBasedOnBlockMeta()) PneumaticCraftUtils.rotateMatrixByMetadata(tile.getBlockMetadata() % 6);
        model.renderModel(0.0625F, tile, f);
        GL11.glPopMatrix();
    }

    /*
     * IItemRenderer part
     */

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
                render(0.5F, -0.5F, 1.5F, 1.0F);
                return;
            }
            case EQUIPPED_FIRST_PERSON: {
                GL11.glRotatef(-90F, 0, 1F, 0);
                render(0.7F, 0.7F, 1.9F, 1.0F);
                return;
            }
            case INVENTORY: {
                render(0.0F, 0.0F, 1.0F, 1.0F);
                return;
            }
            default:
                return;
        }
    }

    private void render(float x, float y, float z, float scale){
        GL11.glPushMatrix();
        GL11.glRotatef(-90F, 1F, 0, 0);
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(-90F, 1F, 0, 0);
        if(model.getModelTexture() != null) FMLClientHandler.instance().getClient().getTextureManager().bindTexture(model.getModelTexture());
        model.renderModel(1F / 16F, null, 0);
        GL11.glPopMatrix();
    }

}
