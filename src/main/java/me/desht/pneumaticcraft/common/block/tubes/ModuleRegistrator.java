package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;

@Mod.EventBusSubscriber
public class ModuleRegistrator {
    public static final HashMap<String, Class<? extends TubeModule>> modules = new HashMap<String, Class<? extends TubeModule>>();
    public static final HashMap<String, Item> moduleItems = new HashMap<String, Item>();
    @SideOnly(Side.CLIENT)
    public static HashMap<Class<? extends TubeModule>, IBakedModel> models;

    @SubscribeEvent
    public static void init(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registerModule(registry, ModuleSafetyValve.class);
        registerModule(registry, ModulePressureGauge.class);
        registerModule(registry, ModuleFlowDetector.class);
        registerModule(registry, ModuleAirGrate.class);
        registerModule(registry, ModuleRegulatorTube.class);
        registerModule(registry, ModuleCharging.class);
        registerModule(registry, ModuleLogistics.class);
    }

    private static void registerModule(IForgeRegistry<Item> registry, Class<? extends TubeModule> moduleClass) {
        try {
            TubeModule module = moduleClass.newInstance();
            modules.put(module.getType(), moduleClass);
            ModInteractionUtils.getInstance().registerModulePart(module.getType());
            Item moduleItem = ModInteractionUtils.getInstance().getModuleItem(module.getType());
            moduleItem.setUnlocalizedName(module.getType());
            Itemss.registerItem(registry, moduleItem);
            moduleItems.put(module.getType(), moduleItem);
        } catch (InstantiationException e) {
            Log.error("Not able to create an instance of the module " + moduleClass.getName() + ". Is the constructor a parameterless one?");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.error("Not able to create an instance of the module " + moduleClass.getName() + ". Is the constructor public?");
            e.printStackTrace();
        }
    }

    public static TubeModule getModule(String moduleName) {
        Class<? extends TubeModule> clazz = modules.get(moduleName);
        if (clazz == null) {
            Log.error("No tube module found for the name \"" + moduleName + "\"!");
            Log.error("Returning a safety valve");
            return new ModuleSafetyValve();
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) { //shouldn't happen anyways, we tested it in the method above.
            e.printStackTrace();
            return null;
        }
    }

    public static Item getModuleItem(String moduleName) {
        return moduleItems.get(moduleName);
    }
}
