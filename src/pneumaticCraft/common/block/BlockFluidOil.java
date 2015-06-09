package pneumaticCraft.common.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import pneumaticCraft.common.fluid.Fluids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFluidOil extends BlockFluidClassic{

    public BlockFluidOil(){
        super(Fluids.oil, new MaterialLiquid(MapColor.blackColor){
            @Override
            public int getMaterialMobility(){
                return 1;
            }
        });
        Fluids.oil.setBlock(this); // Set the fluids block ID to this block.
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta){
        return Fluids.oil.getIcon();
    }

    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister){}
}
