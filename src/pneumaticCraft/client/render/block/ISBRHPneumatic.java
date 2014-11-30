package pneumaticCraft.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public abstract class ISBRHPneumatic implements ISimpleBlockRenderingHandler{
    private final int renderId;

    public ISBRHPneumatic(){
        renderId = RenderingRegistry.getNextAvailableRenderId();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer){}

    @Override
    public boolean shouldRender3DInInventory(int modelId){
        return false;
    }

    @Override
    public int getRenderId(){
        return renderId;
    }
}
