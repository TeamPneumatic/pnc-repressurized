package pneumaticCraft.common.thirdparty.hydraulicraft;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.thirdparty.IThirdParty;

public class Hydraulicraft implements IThirdParty{

    public static Block pneumaticPump;

    @Override
    public String getModId(){
        return "HydCraft";
    }

    @Override
    public void preInit(CreativeTabs pneumaticCraftTab){
        pneumaticPump = new BlockPneumaticPump(Material.iron).setCreativeTab(pneumaticCraftTab).setBlockName("pneumaticPump").setHardness(3.0F).setResistance(3.0F);
        Blockss.registerBlock(pneumaticPump);
    }

    @Override
    public void init(){
        //TODO recipe
    }

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){}

}
