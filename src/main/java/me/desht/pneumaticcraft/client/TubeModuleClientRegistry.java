package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.gui.tubemodule.GuiTubeModule;
import me.desht.pneumaticcraft.client.render.tube_module.TubeModuleRendererBase;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class TubeModuleClientRegistry {
    private static final Map<ResourceLocation, Supplier<? extends TubeModuleRendererBase<?>>> MODEL_FACTORY = new HashMap<>();
    private static final Map<ResourceLocation, Function<? extends TubeModule, ? extends GuiTubeModule<?>>> GUI_FACTORY = new HashMap<>();

    static void registerTubeModuleRenderer(ResourceLocation moduleType, Supplier<? extends TubeModuleRendererBase<?>> factory) {
        MODEL_FACTORY.put(moduleType, factory);
    }

    static <T extends TubeModule> void registerTubeModuleGUI(ResourceLocation moduleType, Function<T, ? extends GuiTubeModule<T>> factory) {
        GUI_FACTORY.put(moduleType, factory);
    }

    public static <T extends TubeModule> GuiTubeModule<T> createGUI(T module) {
        //noinspection unchecked
        Function<T, ? extends GuiTubeModule<T>> factory = (Function<T, ? extends GuiTubeModule<T>>)GUI_FACTORY.get(module.getType());
        return (factory == null) ? null : factory.apply(module);
    }

    public static <T extends TubeModule> TubeModuleRendererBase<T> createModel(T module) {
        //noinspection unchecked
        return ((Supplier<TubeModuleRendererBase<T>>)MODEL_FACTORY.get(module.getType())).get();
    }
}
