package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;

@Mod.EventBusSubscriber
public class ModuleRegistrator {
    private static final HashMap<String, Class<? extends TubeModule>> module2class = new HashMap<>();
    private static final HashMap<String, Item> module2Item = new HashMap<>();

    @SubscribeEvent
    public static void init(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registerModule(registry, new ModuleSafetyValve());
        registerModule(registry, new ModulePressureGauge());
        registerModule(registry, new ModuleFlowDetector());
        registerModule(registry, new ModuleAirGrate());
        registerModule(registry, new ModuleRegulatorTube());
        registerModule(registry, new ModuleCharging());
        registerModule(registry, new ModuleLogistics());
        registerModule(registry, new ModuleRedstone());
    }

    private static void registerModule(IForgeRegistry<Item> registry, TubeModule module) {
        Item moduleItem = new ItemTubeModule(module.getType());
        ModItems.Registration.registerItem(registry, moduleItem);
        module2class.put(module.getType(), module.getClass());
        module2Item.put(module.getType(), moduleItem);
    }

    public static TubeModule getModule(String moduleName) {
        Class<? extends TubeModule> clazz = module2class.get(moduleName);
        if (clazz == null) {
            Log.error("No tube module found for the name \"" + moduleName + "\"!");
            return null;
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // shouldn't happen...
            e.printStackTrace();
            return null;
        }
    }

    public static Item getModuleItem(String moduleName) {
        return module2Item.get(moduleName);
    }
}
