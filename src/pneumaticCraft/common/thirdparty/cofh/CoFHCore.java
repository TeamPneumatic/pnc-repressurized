package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.client.model.ModelThirdPartyCompressor;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.lib.Names;
import pneumaticCraft.proxy.ClientProxy;
import cpw.mods.fml.common.registry.GameRegistry;

public class CoFHCore implements IThirdParty{

    public static Block pneumaticDynamo;
    public static Block fluxCompressor;

    @Override
    public void preInit(){
        pneumaticDynamo = new BlockPneumaticDynamo(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("pneumaticDynamo");
        fluxCompressor = new BlockFluxCompressor(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("fluxCompressor");

        Blockss.registerBlock(pneumaticDynamo);
        Blockss.registerBlock(fluxCompressor);

        GameRegistry.registerTileEntity(TileEntityPneumaticDynamo.class, "PneumaticCraft_pneumaticDynamo");
        GameRegistry.registerTileEntity(TileEntityFluxCompressor.class, "PneumaticCraft_fluxCompressor");

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.compressedIronGear), " i ", "isi", " i ", 'i', Names.INGOT_IRON_COMPRESSED, 's', Items.iron_ingot));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(pneumaticDynamo), " t ", "gig", "ipi", 'i', Names.INGOT_IRON_COMPRESSED, 'g', Itemss.compressedIronGear, 't', Blockss.advancedPressureTube, 'p', Itemss.printedCircuitBoard));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fluxCompressor), "gcp", "frt", "gqp", 'p', Itemss.printedCircuitBoard, 'c', Itemss.compressedIronGear, 'g', Items.redstone, 't', new ItemStack(Blockss.advancedPressureTube, 1, 0), 'r', Itemss.turbineRotor, 'f', Blocks.redstone_block, 'q', Blocks.furnace));

        PneumaticRegistry.getInstance().registerBlockTrackEntry(new BlockTrackEntryRF());
    }

    @Override
    public void init(){}

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){
        ClientProxy.registerBaseModelRenderer(fluxCompressor, TileEntityFluxCompressor.class, new ModelThirdPartyCompressor(ModelThirdPartyCompressor.Type.RF));
        ClientProxy.registerBaseModelRenderer(pneumaticDynamo, TileEntityPneumaticDynamo.class, new ModelPneumaticDynamo());
    }

}
