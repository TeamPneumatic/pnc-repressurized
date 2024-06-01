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

package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.client.pneumatic_armor.ComponentInit;
import me.desht.pneumaticcraft.common.drone.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class ConfigHelper {
    static Fluid cachedXPFluid = null;

    public static ClientConfig client() {
        return ConfigHolder.client;
    }

    public static CommonConfig common() {
        return ConfigHolder.common;
    }

    public static void setProgrammerDifficulty(WidgetDifficulty difficulty) {
        ConfigHolder.client.general.programmerDifficulty.set(difficulty);
    }

    public static void setGuiRemoteGridSnap(boolean snap) {
        ConfigHolder.client.general.guiRemoteGridSnap.set(snap);
    }

    public static void updateCoordTracker(boolean pathEnabled, boolean wirePath, boolean xRayEnabled, ClientConfig.PathUpdateSetting pathUpdateSetting) {
        ConfigHolder.client.armor.pathEnabled.set(pathEnabled);
        ConfigHolder.client.armor.wirePath.set(wirePath);
        ConfigHolder.client.armor.xRayEnabled.set(xRayEnabled);
        ConfigHolder.client.armor.pathUpdateSetting.set(pathUpdateSetting);
    }

    public static void setShowPressureNumerically(boolean numeric) {
        ConfigHolder.client.armor.showPressureNumerically.set(numeric);
    }

    public static void setShowEnchantGlint(boolean show) {
        ConfigHolder.client.armor.showEnchantGlint.set(show);
    }

    public static void setComponentInit(ComponentInit when) {
        ConfigHolder.client.armor.componentInitMessages.set(when);
    }

    public static Fluid getExperienceFluid() {
        if (cachedXPFluid == null) {
            try {
                ResourceLocation id = new ResourceLocation(ConfigHolder.common.machines.defaultXPFluid.get());
                cachedXPFluid = BuiltInRegistries.FLUID.getOrThrow(ResourceKey.create(Registries.FLUID, id));
            } catch (ResourceLocationException | IllegalStateException e) {
                Log.error("invalid resource location {} for 'default_experience_fluid'", ConfigHolder.common.machines.defaultXPFluid.get());
                cachedXPFluid = ModFluids.MEMORY_ESSENCE.get();
            }
        }
        return cachedXPFluid;
    }
}
