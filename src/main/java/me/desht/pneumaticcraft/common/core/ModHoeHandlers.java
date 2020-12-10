package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ModHoeHandlers {
    public static final DeferredRegister<HoeHandler> HOE_HANDLERS_DEFERRED = DeferredRegister.create(HoeHandler.class, Names.MOD_ID);
    public static final Supplier<IForgeRegistry<HoeHandler>> HOE_HANDLERS = HOE_HANDLERS_DEFERRED
            .makeRegistry("hoe_handlers", () -> new RegistryBuilder<HoeHandler>().disableSaving().disableSync());

    public static final RegistryObject<HoeHandler> DEFAULT = HOE_HANDLERS_DEFERRED.register("default_hoe_handler", HoeHandler.DefaultHoeHandler::new);
}
