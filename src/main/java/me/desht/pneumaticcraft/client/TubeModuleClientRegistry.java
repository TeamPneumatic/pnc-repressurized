package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.gui.tubemodule.GuiTubeModule;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class TubeModuleClientRegistry {
    private static final Map<ResourceLocation, Supplier<? extends ModelModuleBase>> MODEL_FACTORY = new HashMap<>();
    private static final Map<ResourceLocation, Function<BlockPos, ? extends GuiTubeModule>> guis = new HashMap<>();

    public static <M extends TubeModule, N extends ModelModuleBase<M>> void registerTubeModuleModel(ResourceLocation moduleType, Supplier<? extends ModelModuleBase> factory) {
        MODEL_FACTORY.put(moduleType, factory);
    }

    public static void registerTubeModuleGUI(ResourceLocation moduleType, Function<BlockPos, ? extends GuiTubeModule> factory) {
        guis.put(moduleType, factory);
    }

    public static GuiTubeModule createGUI(ResourceLocation moduleType, BlockPos pos) {
        return guis.get(moduleType).apply(pos);
    }

    public static ModelModuleBase createModel(TubeModule module) {
        return MODEL_FACTORY.get(module.getType()).get();
    }

}
