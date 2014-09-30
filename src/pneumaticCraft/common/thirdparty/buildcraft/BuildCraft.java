package pneumaticCraft.common.thirdparty.buildcraft;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.client.model.ModelThirdPartyCompressor;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.common.util.PneumaticCraftUtils.EnumBuildcraftModule;
import pneumaticCraft.lib.Names;
import pneumaticCraft.proxy.ClientProxy;
import cpw.mods.fml.common.registry.GameRegistry;

public class BuildCraft implements IThirdParty{

    public static Block pneumaticEngine;
    public static Block kineticCompressor;

    @Override
    public void preInit(){
        pneumaticEngine = new BlockPneumaticEngine(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("pneumaticEngine");
        kineticCompressor = new BlockKineticCompressor(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("kineticCompressor");
        Blockss.registerBlock(pneumaticEngine);
        Blockss.registerBlock(kineticCompressor);
        GameRegistry.registerTileEntity(TileEntityPneumaticEngine.class, "TileEntityPneumaticEngine");
        GameRegistry.registerTileEntity(TileEntityKineticCompressor.class, "TileEntityKineticCompressor");
        PneumaticRegistry.instance.registerBlockTrackEntry(new BlockTrackEntryBCEnergy());
    }

    @Override
    public void init(){
        ItemStack diamondConductivePipe = PneumaticCraftUtils.getBuildcraftItemStack(EnumBuildcraftModule.TRANSPORT, "pipePowerDiamond");
        ItemStack goldGear = PneumaticCraftUtils.getBuildcraftItemStack(EnumBuildcraftModule.CORE, "goldGearItem");
        ItemStack gate = PneumaticCraftUtils.getBuildcraftItemStack(EnumBuildcraftModule.TRANSPORT, "pipeGate");
        ItemStack stoneGear = PneumaticCraftUtils.getBuildcraftItemStack(EnumBuildcraftModule.CORE, "stoneGearItem");

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.compressedIronGear), " i ", "isi", " i ", 'i', Names.INGOT_IRON_COMPRESSED, 's', stoneGear));

        if(Config.enableKineticCompressorRecipe) GameRegistry.addRecipe(new ItemStack(kineticCompressor), "gcp", "frt", "gqp", 'p', Itemss.printedCircuitBoard, 'c', goldGear, 'g', gate, 't', new ItemStack(Blockss.advancedPressureTube, 1, 0), 'r', Itemss.turbineRotor, 'f', diamondConductivePipe, 'q', Blocks.furnace);
        if(Config.enablePneumaticEngineRecipe) GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(pneumaticEngine), "iii", " g ", "cpc", 'i', Names.INGOT_IRON_COMPRESSED, 'c', Itemss.compressedIronGear, 'g', Blocks.glass, 'p', Blocks.piston));
    }

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){
        ClientProxy.registerBaseModelRenderer(kineticCompressor, TileEntityKineticCompressor.class, new ModelThirdPartyCompressor(ModelThirdPartyCompressor.Type.MJ));
        ClientProxy.registerBaseModelRenderer(pneumaticEngine, TileEntityPneumaticEngine.class, new ModelPneumaticEngine());
    }

}
