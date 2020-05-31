package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.gui.tubemodule.GuiTubeModule;
import me.desht.pneumaticcraft.client.render.tube_module.TubeModuleRendererBase;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class TubeModuleClientRegistry {
    private static final Map<ResourceLocation, Supplier<? extends TubeModuleRendererBase>> MODEL_FACTORY = new HashMap<>();
    private static final Map<ResourceLocation, Function<BlockPos, ? extends GuiTubeModule>> guis = new HashMap<>();

    static void registerTubeModuleRenderer(ResourceLocation moduleType, Supplier<? extends TubeModuleRendererBase> factory) {
        MODEL_FACTORY.put(moduleType, factory);
    }

    static void registerTubeModuleGUI(ResourceLocation moduleType, Function<BlockPos, ? extends GuiTubeModule> factory) {
        guis.put(moduleType, factory);
    }

    public static GuiTubeModule createGUI(ResourceLocation moduleType, BlockPos pos) {
        Function<BlockPos, ? extends GuiTubeModule> factory = guis.get(moduleType);
        return factory == null ? null : factory.apply(pos);
    }

    public static TubeModuleRendererBase createModel(TubeModule module) {
        return MODEL_FACTORY.get(module.getType()).get();
    }

}
