package pneumaticCraft.common.fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Fluids{
    public static Fluid EtchAcid;
    public static Fluid plastic;
    public static Fluid oil;
    public static Fluid lpg;
    public static Fluid gasoline;
    public static Fluid kerosene;
    public static Fluid diesel;
    public static List<Fluid> fluids = new ArrayList<Fluid>();
    public static List<Boolean> nativeFluids = new ArrayList<Boolean>();
    public static Map<Block, Item> fluidBlockToBucketMap = new HashMap<Block, Item>();

    public static void initFluids(){
        plastic = new FluidPlastic("plastic");
        EtchAcid = new Fluid("EtchAcid"){
            @Override
            public int getColor(){
                return Blockss.etchingAcid.colorMultiplier(null, 0, 0, 0);
            }
        };

        FluidRegistry.registerFluid(EtchAcid);
        FluidRegistry.registerFluid(plastic);

        oil = new Fluid("oil").setDensity(800).setViscosity(10000);
        lpg = new Fluid("lpg");
        gasoline = new Fluid("fuel");
        kerosene = new Fluid("kerosene");
        diesel = new Fluid("diesel");

        fluids.add(lpg);
        fluids.add(gasoline);
        fluids.add(kerosene);
        fluids.add(diesel);
        fluids.add(oil);

        initializeFluidBlocksAndBuckets();

        lpg = FluidRegistry.getFluid("lpg");
        gasoline = FluidRegistry.getFluid("fuel");
        kerosene = FluidRegistry.getFluid("kerosene");
        diesel = FluidRegistry.getFluid("diesel");
        oil = FluidRegistry.getFluid("oil");

        PneumaticRegistry.getInstance().registerFuel(oil, 150000);
        PneumaticRegistry.getInstance().registerFuel(diesel, 700000);
        PneumaticRegistry.getInstance().registerFuel(kerosene, 1100000);
        PneumaticRegistry.getInstance().registerFuel(gasoline, 1500000);
        PneumaticRegistry.getInstance().registerFuel(lpg, 1800000);

    }

    private static void initializeFluidBlocksAndBuckets(){
        for(final Fluid fluid : fluids) {
            boolean nativeFluid = FluidRegistry.getFluid(fluid.getName()) == null;
            nativeFluids.add(nativeFluid);
            if(nativeFluid) {
                FluidRegistry.registerFluid(fluid);
            }
            Block fluidBlock = fluid.getBlock();
            if(fluidBlock == null) {
                fluidBlock = new BlockFluidClassic(fluid, new MaterialLiquid(MapColor.waterColor)){
                    private IIcon flowingIcon, stillIcon;

                    @Override
                    public void registerBlockIcons(IIconRegister register){
                        flowingIcon = register.registerIcon("pneumaticcraft:" + fluid.getName() + "_flow");
                        stillIcon = register.registerIcon("pneumaticcraft:" + fluid.getName() + "_still");
                    }

                    @Override
                    @SideOnly(Side.CLIENT)
                    public IIcon getIcon(int side, int meta){
                        return side != 0 && side != 1 ? flowingIcon : stillIcon;
                    }
                }.setBlockName(fluid.getName());
                Blockss.registerBlock(fluidBlock);
                fluid.setBlock(fluidBlock);
            }

            Item fluidBucket = new ItemBucket(fluidBlock).setCreativeTab(PneumaticCraft.tabPneumaticCraft).setTextureName(Textures.ICON_LOCATION + fluid.getName() + "Bucket").setUnlocalizedName(fluid.getName() + "Bucket");
            Itemss.registerItem(fluidBucket);

            fluidBlockToBucketMap.put(fluidBlock, fluidBucket);

            FluidContainerRegistry.registerFluidContainer(new FluidStack(fluid, 1000), new ItemStack(fluidBucket), new ItemStack(Items.bucket));
        }
    }
}
