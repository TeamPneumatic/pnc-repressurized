package pneumaticCraft.common.thirdparty.computercraft;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IRegistryListener;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Names;
import pneumaticCraft.proxy.ClientProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class ComputerCraft implements IThirdParty, IRegistryListener{
    public Block droneInterface;

    @Override
    public void preInit(){
        ThirdPartyManager.computerCraftLoaded = true;
        droneInterface = new BlockDroneInterface(Material.iron).setBlockName("droneInterface");
        Blockss.registerBlock(droneInterface);
        GameRegistry.registerTileEntity(TileEntityDroneInterface.class, "droneInterface");
        TileEntityProgrammer.registeredWidgets.add(new ProgWidgetCC());
        PneumaticRegistry.instance.registerBlockTrackEntry(new BlockTrackEntryPeripheral());
    }

    @Override
    public void init(){
        Block modem = GameRegistry.findBlock(ModIds.COMPUTERCRAFT, "CC-Peripheral");
        if(modem != null) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(droneInterface), true, " u ", "mp ", "iii", 'u', new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_RANGE), 'm', new ItemStack(modem, 1, 1), 'p', Itemss.printedCircuitBoard, 'i', Names.INGOT_IRON_COMPRESSED));
        } else {
            Log.error("Wireless Modem block not found! Using the backup recipe");
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(droneInterface), true, " u ", "mp ", "iii", 'u', new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_RANGE), 'm', Items.ender_pearl, 'p', Itemss.printedCircuitBoard, 'i', Names.INGOT_IRON_COMPRESSED));
        }
    }

    @Override
    public void postInit(){

    }

    @Override
    public void clientSide(){
        ClientProxy.registerBaseModelRenderer(droneInterface, TileEntityDroneInterface.class, new ModelDroneInterface());
    }

    @Override
    public void onItemRegistry(Item item){}

    @Override
    public void onBlockRegistry(Block block){
        if(block instanceof IPeripheralProvider) {
            ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider)block);
        }
    }

    @Override
    public void clientInit(){}

}
