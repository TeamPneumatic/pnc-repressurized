package pneumaticCraft.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderPneumaticDoorCamo implements ISimpleBlockRenderingHandler{

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer){

    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block b, int modelId, RenderBlocks renderer){
        TileEntity tile = world.getTileEntity(x, y, z);
        if(tile instanceof TileEntityPneumaticDoorBase) {
            TileEntityPneumaticDoorBase door = (TileEntityPneumaticDoorBase)tile;
            ItemStack camoStack = door.getStackInSlot(TileEntityPneumaticDoorBase.CAMO_SLOT);
            if(camoStack != null && camoStack.getItem() instanceof ItemBlock) {
                Block block = Block.getBlockFromItem(camoStack.getItem());
                if(PneumaticCraftUtils.isRenderIDCamo(block.getRenderType())) {
                    renderer.renderBlockAllFaces(block, x, y, z);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId){
        return false;
    }

    @Override
    public int getRenderId(){
        return PneumaticCraft.proxy.CAMO_RENDER_ID;
    }

}
