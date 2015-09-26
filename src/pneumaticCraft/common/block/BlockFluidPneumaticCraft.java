package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockFluidPneumaticCraft extends BlockFluidClassic{
    public IIcon flowingIcon, stillIcon;

    public BlockFluidPneumaticCraft(Fluid fluid, Material material){
        super(fluid, material);
        setBlockName(fluid.getName());
    }

    public BlockFluidPneumaticCraft(Fluid fluid){
        this(fluid, Material.water);
    }

    @Override
    public void registerBlockIcons(IIconRegister register){
        flowingIcon = register.registerIcon("pneumaticcraft:" + getFluid().getName() + "_flow");
        stillIcon = register.registerIcon("pneumaticcraft:" + getFluid().getName() + "_still");
    }

    @Override
    public IIcon getIcon(int side, int meta){
        return side != 0 && side != 1 ? flowingIcon : stillIcon;
    }
}
