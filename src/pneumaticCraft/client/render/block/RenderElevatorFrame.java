package pneumaticCraft.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.common.block.BlockElevatorFrame;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class RenderElevatorFrame extends ISBRHPneumatic{

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
        boolean frameXPos = world.getBlock(x + 1, y, z) == Blockss.elevatorFrame;
        boolean frameXNeg = world.getBlock(x - 1, y, z) == Blockss.elevatorFrame;
        boolean frameZPos = world.getBlock(x, y, z + 1) == Blockss.elevatorFrame;
        boolean frameZNeg = world.getBlock(x, y, z - 1) == Blockss.elevatorFrame;
        TileEntityElevatorBase base = BlockElevatorFrame.getElevatorTE(world, x, y, z);
        renderer.renderAllFaces = true;
        if(base != null && base.frameCamo != null && PneumaticCraftUtils.isRenderIDCamo(base.frameCamo.getRenderType())) {
            renderer.setOverrideBlockTexture(base.frameCamo.getIcon(0, base.getStackInSlot(5).getItemDamage()));
        }
        if(!frameXPos && !frameZPos) {
            renderer.setRenderBounds(14 / 16D, 0, 14 / 16D, 15 / 16D, 1, 15 / 16D);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if(!frameXNeg && !frameZPos) {
            renderer.setRenderBounds(1 / 16D, 0, 14 / 16D, 2 / 16D, 1, 15 / 16D);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if(!frameXPos && !frameZNeg) {
            renderer.setRenderBounds(14 / 16D, 0, 1 / 16D, 15 / 16D, 1, 2 / 16D);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if(!frameXNeg && !frameZNeg) {
            renderer.setRenderBounds(1 / 16D, 0, 1 / 16D, 2 / 16D, 1, 2 / 16D);
            renderer.renderStandardBlock(block, x, y, z);
        }
        renderer.setOverrideBlockTexture(null);
        renderer.renderAllFaces = false;
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer){
        RenderUtils.RenderInfo renderInfo = new RenderUtils.RenderInfo(0, 0, 0, 1, 1, 1);
        renderInfo.baseBlock = block;

        GL11.glPushMatrix();
        GL11.glTranslated(-0.5, -0.5, -0.5);

        renderInfo.setBounds(14 / 16D, 0, 14 / 16D, 15 / 16D, 1, 15 / 16D);
        RenderUtils.INSTANCE.renderBlock(renderInfo, null, 0, 0, 0, false, true);

        renderInfo.setBounds(1 / 16D, 0, 14 / 16D, 2 / 16D, 1, 15 / 16D);
        RenderUtils.INSTANCE.renderBlock(renderInfo, null, 0, 0, 0, false, true);

        renderInfo.setBounds(14 / 16D, 0, 1 / 16D, 15 / 16D, 1, 2 / 16D);
        RenderUtils.INSTANCE.renderBlock(renderInfo, null, 0, 0, 0, false, true);

        renderInfo.setBounds(1 / 16D, 0, 1 / 16D, 2 / 16D, 1, 2 / 16D);
        RenderUtils.INSTANCE.renderBlock(renderInfo, null, 0, 0, 0, false, true);

        GL11.glPopMatrix();
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId){
        return true;
    }

}
