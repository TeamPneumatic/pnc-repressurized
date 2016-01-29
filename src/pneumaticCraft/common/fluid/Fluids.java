package pneumaticCraft.common.fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.block.BlockFluidEtchingAcid;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemPneumatic;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Fluids{
    public static final Fluid etchingAcid = new FluidPneumaticCraft("etchacid", false){
        @Override
        public int getColor(){
            if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                return getBlock().colorMultiplier(null, 0, 0, 0);
            } else {
                return super.getColor();
            }
        }
    };
    public static final Fluid plastic = new FluidPlastic("plastic");
    public static final Fluid oil = new FluidPneumaticCraft("oil").setDensity(800).setViscosity(10000);
    public static final Fluid lpg = new FluidPneumaticCraft("lpg");
    public static final Fluid gasoline = new FluidPneumaticCraft("fuel");
    public static final Fluid kerosene = new FluidPneumaticCraft("kerosene");
    public static final Fluid diesel = new FluidPneumaticCraft("diesel");
    public static final Fluid lubricant = new FluidPneumaticCraft("lubricant");
    public static List<Fluid> fluids = new ArrayList<Fluid>();
    public static Map<Block, Item> fluidBlockToBucketMap = new HashMap<Block, Item>();
    private static Map<String, Block> fluidToBlockMap = new HashMap<String, Block>();//you could theoretically use fluid.getBlock(), but other mods like GregTech break it for some reason.

    public static void initFluids(){

        etchingAcid.setBlock(new BlockFluidEtchingAcid());
        plastic.getBlock().setBlockName(plastic.getName() + "Block");

        fluids.add(plastic);
        fluids.add(etchingAcid);
        fluids.add(lpg);
        fluids.add(gasoline);
        fluids.add(kerosene);
        fluids.add(diesel);
        fluids.add(oil);
        fluids.add(lubricant);

        initializeFluidBlocksAndBuckets();

        PneumaticRegistry.getInstance().registerFuel(oil, 150000);
        PneumaticRegistry.getInstance().registerFuel(diesel, 700000);
        PneumaticRegistry.getInstance().registerFuel(kerosene, 1100000);
        PneumaticRegistry.getInstance().registerFuel(gasoline, 1500000);
        PneumaticRegistry.getInstance().registerFuel(lpg, 1800000);

        PneumaticCraft.instance.registerFuel(new ItemStack(getBucket(oil)), 150000 / 2);
        PneumaticCraft.instance.registerFuel(new ItemStack(getBucket(diesel)), 700000 / 2);
        PneumaticCraft.instance.registerFuel(new ItemStack(getBucket(kerosene)), 1100000 / 2);
        PneumaticCraft.instance.registerFuel(new ItemStack(getBucket(gasoline)), 1500000 / 2);
        PneumaticCraft.instance.registerFuel(new ItemStack(getBucket(lpg)), 1800000 / 2);

    }

    public static boolean areFluidsEqual(Fluid fluid1, Fluid fluid2){
        if(fluid1 == null && fluid2 == null) return true;
        if(fluid1 == null != (fluid2 == null)) return false;
        return fluid1.getName().equals(fluid2.getName());
    }

    public static Item getBucket(Fluid fluid){
        return fluidBlockToBucketMap.get(getBlock(fluid));
    }

    public static Block getBlock(Fluid fluid){
        return fluidToBlockMap.get(fluid.getName());
    }

    private static void initializeFluidBlocksAndBuckets(){
        for(final Fluid fluid : fluids) {
            //FluidRegistry.registerFluid(fluid); (The constructor of FluidPneumaticCrafts registers the fluid.
            Block fluidBlock = fluid.getBlock();
            Blockss.registerBlock(fluidBlock);
            fluidToBlockMap.put(fluid.getName(), fluidBlock);

            Item fluidBucket = new ItemBucket(fluidBlock){
                @Override
                public void addInformation(ItemStack p_77624_1_, net.minecraft.entity.player.EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_){
                    super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
                    ItemPneumatic.addTooltip(p_77624_1_, p_77624_2_, p_77624_3_);
                };

                @Override
                @SideOnly(Side.CLIENT)
                public void getSubItems(Item item, CreativeTabs creativeTab, List items){
                    if(FluidRegistry.isFluidDefault(fluid)) super.getSubItems(item, creativeTab, items);
                }
            }.setContainerItem(Items.bucket).setCreativeTab(PneumaticCraft.tabPneumaticCraft).setTextureName(Textures.ICON_LOCATION + fluid.getName() + "Bucket").setUnlocalizedName(fluid.getName() + "Bucket");

            Itemss.registerItem(fluidBucket);

            fluidBlockToBucketMap.put(fluidBlock, fluidBucket);

            FluidContainerRegistry.registerFluidContainer(new FluidStack(fluid, 1000), new ItemStack(fluidBucket), new ItemStack(Items.bucket));
        }
    }
}
