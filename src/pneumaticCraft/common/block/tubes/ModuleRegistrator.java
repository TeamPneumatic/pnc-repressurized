package pneumaticCraft.common.block.tubes;

import java.util.HashMap;

import net.minecraft.item.Item;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.lib.Log;

public class ModuleRegistrator{
    public static HashMap<String, Class<? extends TubeModule>> modules = new HashMap<String, Class<? extends TubeModule>>();
    public static HashMap<String, Item> moduleItems = new HashMap<String, Item>();

    public static void init(){
        registerModule(ModuleSafetyValve.class);
        registerModule(ModulePressureGauge.class);
        registerModule(ModuleFlowDetector.class);
        registerModule(ModuleAirGrate.class);
        registerModule(ModuleRegulatorTube.class);
        registerModule(ModuleCharging.class);
        registerModule(ModuleLogistics.class);
    }

    public static void registerModule(Class<? extends TubeModule> moduleClass){
        try {
            TubeModule module = moduleClass.newInstance();
            modules.put(module.getType(), moduleClass);
            ModInteractionUtils.getInstance().registerModulePart(module.getType());
            Item moduleItem = ModInteractionUtils.getInstance().getModuleItem(module.getType());
            moduleItem.setUnlocalizedName(module.getType());
            Itemss.registerItem(moduleItem);
            moduleItems.put(module.getType(), moduleItem);
        } catch(InstantiationException e) {
            Log.error("Not able to create an instance of the module " + moduleClass.getName() + ". Is the constructor a parameterless one?");
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            Log.error("Not able to create an instance of the module " + moduleClass.getName() + ". Is the constructor public?");
            e.printStackTrace();
        }
    }

    public static TubeModule getModule(String moduleName){
        Class<? extends TubeModule> clazz = modules.get(moduleName);
        if(clazz == null) {
            Log.error("No tube module found for the name \"" + moduleName + "\"!");
            Log.error("Returning a safety valve");
            return new ModuleSafetyValve();
        }
        try {
            return clazz.newInstance();
        } catch(InstantiationException e) {//shouldn't happen anyways, we tested it in the method above.
            e.printStackTrace();
            return null;
        } catch(IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Item getModuleItem(String moduleName){
        return moduleItems.get(moduleName);
    }
}
