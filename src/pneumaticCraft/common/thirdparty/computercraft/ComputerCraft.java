package pneumaticCraft.common.thirdparty.computercraft;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IRegistryListener;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Names;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class ComputerCraft extends OpenComputers implements IRegistryListener{

    @Override
    public void preInit(){
        ThirdPartyManager.computerCraftLoaded = true;
        PneumaticRegistry.getInstance().registerBlockTrackEntry(new BlockTrackEntryPeripheral());
        super.preInit();
    }

    @Override
    public void init(){
        if(Loader.isModLoaded(ModIds.OPEN_COMPUTERS)) super.init();
        Block modem = GameRegistry.findBlock(ModIds.COMPUTERCRAFT, "CC-Peripheral");
        if(modem != null) {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(droneInterface), true, " u ", "mp ", "iii", 'u', new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_RANGE), 'm', new ItemStack(modem, 1, 1), 'p', Itemss.printedCircuitBoard, 'i', Names.INGOT_IRON_COMPRESSED));
        } else {
            Log.error("Wireless Modem block not found! Using the backup recipe");
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(droneInterface), true, " u ", "mp ", "iii", 'u', new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_RANGE), 'm', Items.ender_pearl, 'p', Itemss.printedCircuitBoard, 'i', Names.INGOT_IRON_COMPRESSED));
        }
    }

    @Override
    public void onItemRegistry(Item item){}

    @Override
    public void onBlockRegistry(Block block){
        if(block instanceof IPeripheralProvider) {
            ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider)block);
        }
    }

}
