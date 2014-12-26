package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.client.model.ModelThirdPartyCompressor;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.lib.Names;
import pneumaticCraft.proxy.ClientProxy;
import pneumaticCraft.proxy.CommonProxy;
import cofh.api.energy.IEnergyStorage;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class CoFHCore implements IThirdParty, IGuiHandler{

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
        PneumaticRegistry.getInstance().registerCustomBlockInteractor(new DroneInteractRFExport());
        PneumaticRegistry.getInstance().registerCustomBlockInteractor(new DroneInteractRFImport());
        TileEntityProgrammer.registeredWidgets.add(new ProgWidgetRFCondition());
        TileEntityProgrammer.registeredWidgets.add(new ProgWidgetDroneConditionRF());

        MinecraftForge.EVENT_BUS.register(this);
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

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        if(ID == CommonProxy.GUI_ID_PNEUMATIC_DYNAMO || ID == CommonProxy.GUI_ID_FLUX_COMPRESSOR) return new ContainerRF(player.inventory, (TileEntityPneumaticBase)world.getTileEntity(x, y, z));
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        switch(ID){
            case CommonProxy.GUI_ID_PNEUMATIC_DYNAMO:
                return new GuiPneumaticDynamo(player.inventory, (TileEntityPneumaticDynamo)world.getTileEntity(x, y, z));
            case CommonProxy.GUI_ID_FLUX_COMPRESSOR:
                return new GuiFluxCompressor(player.inventory, (TileEntityFluxCompressor)world.getTileEntity(x, y, z));
        }
        return null;
    }

    @Override
    public void clientInit(){}

    @SubscribeEvent
    public void onEntityConstruction(EntityConstructing event){
        if(event.entity instanceof IDrone) {
            getEnergyStorage((EntityCreature)event.entity);//will add an instance of ExtendedEntityProperties that can be loaded out of NBT.
        }
    }

    public static IEnergyStorage getEnergyStorage(EntityCreature entity){
        ExtendedPropertyRF property = (ExtendedPropertyRF)entity.getExtendedProperties("PneumaticCraft_RF");
        if(property == null) {
            property = new ExtendedPropertyRF();
            entity.registerExtendedProperties("PneumaticCraft_RF", property);
        }
        return property.energy;
    }
}
