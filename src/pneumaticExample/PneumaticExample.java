package pneumaticExample;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.block.BlockSupplier;
import pneumaticCraft.api.client.pneumaticHelmet.RenderHandlerRegistry;
import pneumaticCraft.api.item.ItemSupplier;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.api.universalSensor.SensorRegistrator;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "PneumaticExample", name = "PneumaticExample", dependencies = "required-after:PneumaticCraft@1.1.7", version = "1.0.2")
public class PneumaticExample{
    @Instance("PneumaticExample")
    public static PneumaticExample instance;

    @SidedProxy(clientSide = "pneumaticExample.ClientProxy", serverSide = "pneumaticExample.CommonProxy")
    public static CommonProxy proxy;

    public static Block pneumaticDiamondBlock;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        proxy.doClientOnlyStuff();

        pneumaticDiamondBlock = new BlockContainer(Material.iron){
            @Override
            public TileEntity createNewTileEntity(World world, int meta){
                return new TileEntityPneumaticDiamond();
            }

            @Override
            public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ){
                ((IPneumaticMachine)world.getTileEntity(x, y, z)).getAirHandler().onNeighborChange();
            }
        };
        pneumaticDiamondBlock.setBlockName("diamond_block").setCreativeTab(CreativeTabs.tabBlock);

        GameRegistry.registerBlock(pneumaticDiamondBlock, "pneumaticDiamond");
        GameRegistry.registerTileEntity(TileEntityPneumaticDiamond.class, "pneumaticDiamond");
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        //Registering a rendering handler. This will be noticable when you equip a Pneumatic Helmet (regardless of the upgrades).
        RenderHandlerRegistry.renderHandlers.add(new RenderHandlerExample());

        //Adding an Assembly Recipe. In this example the Drill program can be used to drill an Electrostatic Compressor into one Lightning Plant.
        //A metadata of 6 is used here as the Lightning plant seed has a metadata of 6. These are mapped the same as the dye colors.
        AssemblyRecipe.addDrillRecipe(BlockSupplier.getBlock("electrostaticCompressor"), new ItemStack(ItemSupplier.getItem("plasticPlant"), 1, 6));

        //Registering a new sensor (which tracks diamonds in this example.
        SensorRegistrator.sensorRegistrator.registerSensor(new DiamondSensor());

    }

    @EventHandler
    public void PostInit(FMLPostInitializationEvent event){

    }
}
