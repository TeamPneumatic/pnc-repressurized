package pneumaticCraft.common.fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
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
    public static List<Fluid> textureRegisteredFluids = new ArrayList<Fluid>();
    public static Map<Block, Item> fluidBlockToBucketMap = new HashMap<Block, Item>();
    public static boolean isUsingNativeOil;

    public static void initFluids(){
        plastic = new FluidPlastic("plastic");
        EtchAcid = new Fluid("EtchAcid"){
            @Override
            public int getColor(){
                return Blockss.etchingAcid.colorMultiplier(null, 0, 0, 0);
            }
        };
        lpg = new Fluid("lpg");
        gasoline = new Fluid("gasoline");
        kerosene = new Fluid("kerosene");
        diesel = new Fluid("diesel");

        if(!FluidRegistry.isFluidRegistered("oil")) {
            oil = new Fluid("oil").setDensity(800).setViscosity(10000);
            FluidRegistry.registerFluid(oil);
            isUsingNativeOil = true;
        } else {
            oil = FluidRegistry.getFluid("oil");
        }

        FluidRegistry.registerFluid(EtchAcid);
        FluidRegistry.registerFluid(plastic);
        FluidRegistry.registerFluid(lpg);
        FluidRegistry.registerFluid(gasoline);
        FluidRegistry.registerFluid(kerosene);
        FluidRegistry.registerFluid(diesel);

        textureRegisteredFluids.add(lpg);
        textureRegisteredFluids.add(gasoline);
        textureRegisteredFluids.add(kerosene);
        textureRegisteredFluids.add(diesel);

        initializeFluidBlocksAndBuckets();
    }

    private static void initializeFluidBlocksAndBuckets(){
        for(final Fluid fluid : textureRegisteredFluids) {
            Block fluidBlock = new BlockFluidClassic(fluid, new MaterialLiquid(MapColor.waterColor)){
                @Override
                @SideOnly(Side.CLIENT)
                public IIcon getIcon(int side, int meta){
                    return side != 0 && side != 1 ? fluid.getFlowingIcon() : fluid.getStillIcon();
                }
            }.setBlockName(fluid.getName());
            Blockss.registerBlock(fluidBlock);
            fluid.setBlock(fluidBlock);

            Item fluidBucket = new ItemBucket(fluidBlock).setCreativeTab(PneumaticCraft.tabPneumaticCraft).setTextureName(Textures.ICON_LOCATION + fluid.getName() + "Bucket").setUnlocalizedName(fluid.getName() + "Bucket");
            Itemss.registerItem(fluidBucket);

            fluidBlockToBucketMap.put(fluidBlock, fluidBucket);

            FluidContainerRegistry.registerFluidContainer(new FluidStack(fluid, 1000), new ItemStack(fluidBucket), new ItemStack(Items.bucket));
        }
    }
}
