package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModuleRegistrator {
    private static final Map<ResourceLocation, Supplier<? extends TubeModule>> moduleFactory = new HashMap<>();

    @SubscribeEvent
    public static void init(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registerModule(registry, ModuleSafetyValve::new);
        registerModule(registry, ModulePressureGauge::new);
        registerModule(registry, ModuleFlowDetector::new);
        registerModule(registry, ModuleAirGrate::new);
        registerModule(registry, ModuleRegulatorTube::new);
        registerModule(registry, ModuleCharging::new);
        registerModule(registry, ModuleLogistics::new);
        registerModule(registry, ModuleRedstone::new);
    }

    private static void registerModule(IForgeRegistry<Item> registry, Supplier<? extends TubeModule> moduleSupplier) {
        String moduleType = moduleSupplier.get().getType().toString();
        Item moduleItem = new ItemTubeModule(moduleType);
        ModItems.Registration.registerItem(registry, moduleItem);
        moduleFactory.put(moduleItem.getRegistryName(), moduleSupplier);
    }

    public static TubeModule createModule(ResourceLocation moduleName) {
        Validate.isTrue(moduleFactory.containsKey(moduleName), "No tube module found for '" + moduleName + "' (forgot to register it?)");
        return moduleFactory.get(moduleName).get();
    }
}
