package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiCreativeCompressor;
import pneumaticCraft.common.tileentity.TileEntityCreativeCompressor;
import cpw.mods.fml.common.FMLCommonHandler;

public class BlockCreativeCompressor extends BlockPneumaticCraft{

    protected BlockCreativeCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityCreativeCompressor.class;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        if(player.isSneaking()) return false;
        else {
            if(world.isRemote) {
                TileEntity te = world.getTileEntity(x, y, z);
                if(te instanceof TileEntityCreativeCompressor) {
                    FMLCommonHandler.instance().showGuiScreen(new GuiCreativeCompressor((TileEntityCreativeCompressor)te));
                }
            }
            return true;
        }
    }

}
