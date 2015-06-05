package pneumaticCraft.common.thirdparty.nei;

import net.minecraft.item.ItemStack;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.Versions;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.guihook.GuiContainerManager;

public class NEIPluginInitConfig implements IConfigureNEI{

    @Override
    public void loadConfig(){

        Log.info("Initializing " + getName() + "...");

        //Pressure Chamber handler
        API.registerUsageHandler(new NEIPressureChamberRecipeManager());
        API.registerRecipeHandler(new NEIPressureChamberRecipeManager());

        //Assembly Controller Handler
        API.registerUsageHandler(new NEIAssemblyControllerRecipeManager());
        API.registerRecipeHandler(new NEIAssemblyControllerRecipeManager());

        //Thermopneumatic handler
        API.registerRecipeHandler(new NEIThermopneumaticProcessingPlantManager());
        API.registerUsageHandler(new NEIThermopneumaticProcessingPlantManager());

        GuiContainerManager.addDrawHandler(new ItemDrawHandler());

        //handle drop down windows
        /* MultiItemRange tubes = new MultiItemRange();
         tubes.add(Blockss.pressureTube, 0, BlockPressureTube.PRESSURE_TUBES_AMOUNT);
         tubes.add(Blockss.advancedPressureTube, 0, BlockPressureTube.PRESSURE_TUBES_AMOUNT);
         API.addSetRange("Mod.PneumaticCraft.Pressure Tubes", tubes);

         MultiItemRange upgrades = new MultiItemRange();
         upgrades.add(Itemss.machineUpgrade, 0, ItemMachineUpgrade.UPGRADES_AMOUNT);
         API.addSetRange("Mod.PneumaticCraft.Machine Upgrades", upgrades);*/

        API.hideItem(new ItemStack(Blockss.burstPlant));
        API.hideItem(new ItemStack(Blockss.chopperPlant));
        API.hideItem(new ItemStack(Blockss.creeperPlant));
        API.hideItem(new ItemStack(Blockss.enderPlant));
        API.hideItem(new ItemStack(Blockss.fireFlower));
        API.hideItem(new ItemStack(Blockss.flyingFlower));
        API.hideItem(new ItemStack(Blockss.heliumPlant));
        API.hideItem(new ItemStack(Blockss.lightningPlant));
        API.hideItem(new ItemStack(Blockss.potionPlant));
        API.hideItem(new ItemStack(Blockss.propulsionPlant));
        API.hideItem(new ItemStack(Blockss.rainPlant));
        API.hideItem(new ItemStack(Blockss.repulsionPlant));
        API.hideItem(new ItemStack(Blockss.slimePlant));
        API.hideItem(new ItemStack(Blockss.squidPlant));
    }

    @Override
    public String getName(){
        return "PneumaticCraft built-in NEI plugin";
    }

    @Override
    public String getVersion(){
        return Versions.fullVersionString();
    }
}
