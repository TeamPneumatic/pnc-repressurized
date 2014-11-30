package pneumaticCraft.common.thirdparty.hydraulicraft;

import k4unl.minecraft.Hydraulicraft.api.IHydraulicraftRegistrar;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;

public class Hydraulicraft implements IThirdParty{

    public static Block pneumaticPump;

    @Override
    public void preInit(){
        FMLInterModComms.sendMessage(ModIds.HC, "pneumaticCraft.common.thirdparty.hydraulicraft.Hydraulicraft", "registrarHandling");
        //pneumaticPump = new BlockPneumaticPump(Material.iron).setCreativeTab(pneumaticCraftTab).setBlockName("pneumaticPump").setHardness(3.0F).setResistance(3.0F);
        //Blockss.registerBlock(pneumaticPump);
        //GameRegistry.registerTileEntity(TileEntityPneumaticPump.class, "pneumaticPump");
        PneumaticRegistry.instance.registerBlockTrackEntry(new BlockTrackEntryHydraulicraft());
    }

    @Override
    public void init(){}

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){}

    public static void registrarHandling(IHydraulicraftRegistrar registrar){
        registrar.registerTrolley(new TrolleyPlasticPlants());

        ItemStack cropsTrolly = registrar.getTrolleyItem("plasticPlants");
        cropsTrolly.stackSize = 4;

        Block pressureCore = GameData.getBlockRegistry().getObject(ModIds.HC + ":LPBlockCore");
        Block pressureWall = GameData.getBlockRegistry().getObject(ModIds.HC + ":hydraulicPressureWall");
        Block hydraulicPiston = GameData.getBlockRegistry().getObject(ModIds.HC + ":hydraulicPiston");

        GameRegistry.addRecipe(new ShapedOreRecipe(cropsTrolly, true, "-P-", "WCW", "-H-", 'C', new ItemStack(pressureCore, 1, 1), 'W', pressureWall, 'H', Itemss.turbineRotor, 'P', hydraulicPiston));
    }

    @Override
    public void clientInit(){}

}
