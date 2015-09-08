package pneumaticCraft.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;

public class RenderChargingStationPad extends ISBRHPneumatic{

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityChargingStation) {
            TileEntityChargingStation station = (TileEntityChargingStation)te;
            if(station.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0) {
                ItemStack camo = station.getCamoStack();
                Block camoBlock = ((ItemBlock)camo.getItem()).field_150939_a;
                renderer.renderAllFaces = true;
                renderer.setOverrideBlockTexture(camoBlock.getIcon(0, camo.getItemDamage()));
                renderer.setRenderBounds(0, 15 / 16D, 0, 1, 1, 1);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setOverrideBlockTexture(null);
                renderer.renderAllFaces = false;
                return true;
            }
        }
        return false;
    }

}
