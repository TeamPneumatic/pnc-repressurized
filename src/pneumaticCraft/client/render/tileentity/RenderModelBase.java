package pneumaticCraft.client.render.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.model.BaseModel;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderModelBase extends TileEntitySpecialRenderer implements IItemRenderer, ISimpleBlockRenderingHandler{

    private static final Map<TileEntity, Integer> renderLists = new HashMap<TileEntity, Integer>();
    private static final List<TileEntity> tilesRequiringRerender = new ArrayList<TileEntity>();
    private IBaseModel model;

    public RenderModelBase(IBaseModel model){
        this.model = model;
    }

    public RenderModelBase(){}

    /*
     * TileEntitySpecialRenderer part
     */

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f){
        renderModelAt(tileentity, d0, d1, d2, f);
    }

    public void renderModelAt(TileEntity tile, double d, double d1, double d2, float f){
        GL11.glPushMatrix();
        {
            if(model.getModelTexture(tile) != null) FMLClientHandler.instance().getClient().getTextureManager().bindTexture(model.getModelTexture(tile));
            GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
            GL11.glScalef(1.0F, -1F, -1F);
            if(model.rotateModelBasedOnBlockMeta()) {
                PneumaticCraftUtils.rotateMatrixByMetadata(tile.getBlockMetadata() % 6);
            } else {
                PneumaticCraftUtils.rotateMatrixByMetadata(2);
            }

            //TODO refactor when all models are converted:
            if(model instanceof BaseModel) {
                GL11.glTranslated(0, 24 / 16D, 0);
                GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            }

            model.renderDynamic(0.0625F, tile, f);

            //Get the right render list
            Integer renderList = renderLists.get(tile);
            if(renderList == null) {
                renderList = GL11.glGenLists(1);
                renderLists.put(tile, renderList);
                tilesRequiringRerender.add(tile);
            }

            //Rerender onto the list if necessary
            /* if(tilesRequiringRerender.contains(tile)) {
                 tilesRequiringRerender.remove(tile);
                 GL11.glNewList(renderList, GL11.GL_COMPILE);*/
            GL11.glPushMatrix();
            {
                model.renderStatic(0.0625F, tile);
            }
            GL11.glPopMatrix();
            /*  GL11.glEndList();
            }

            //and actually render the static render
            GL11.glPushMatrix();
            GL11.glCallList(renderList);
            GL11.glPopMatrix();*/
        }
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
        if(model.getModelTexture(null) != null) FMLClientHandler.instance().getClient().getTextureManager().bindTexture(model.getModelTexture(null));
        //TODO refactor when all models are converted:
        if(model instanceof BaseModel) {
            GL11.glTranslated(0, 24 / 16D, 0);
            GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
        }
        model.rotateModelBasedOnBlockMeta();
        model.renderDynamic(0.0625F, null, 0);
        model.renderStatic(1F / 16F, null);
        GL11.glPopMatrix();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer){

    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te != null) tilesRequiringRerender.add(te);
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId){
        return false;
    }

    @Override
    public int getRenderId(){
        return PneumaticCraft.proxy.SPECIAL_RENDER_TYPE_VALUE;
    }

}
