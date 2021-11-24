/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

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
