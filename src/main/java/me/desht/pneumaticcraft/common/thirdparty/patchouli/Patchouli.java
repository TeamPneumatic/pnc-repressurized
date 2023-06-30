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

package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.thirdparty.IDocsProvider;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class Patchouli implements IThirdParty, IDocsProvider {
    static final ResourceLocation PNC_BOOK = RL("book");

    private static Screen prevGui;
    private static Screen bookGui;

    @Override
    public void clientInit() {
        MinecraftForge.EVENT_BUS.register(ScreenListener.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(ConfigListener.class);

        PatchouliAccess.setup();
    }

    @Override
    public void showWidgetDocs(String path) {
        Screen prev = Minecraft.getInstance().screen;  // should be the programmer GUI
        if (PatchouliAccess.openBookEntry(RL("programming/" + path))) {
            bookGui = Minecraft.getInstance().screen;
            prevGui = prev;
        }
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public ThirdPartyManager.ModType modType() {
        return ThirdPartyManager.ModType.DOCUMENTATION;
    }

    private static class ConfigListener {
        @SubscribeEvent
        public static void onConfigChange(ModConfigEvent.Reloading event) {
            if (event.getConfig().getModId().equals(Names.MOD_ID)) {
                PatchouliAccess.setConfigFlags();
            }
        }
    }

    private static class ScreenListener {
        private static Runnable scheduledTask = null;

        @SubscribeEvent
        public static void onGuiClosing(ScreenEvent.Closing event) {
            if (prevGui != null && bookGui != null) {
                // reopen the programmer GUI if that's where we came from
                scheduledTask = () -> {
                    Minecraft.getInstance().setScreen(prevGui);
                    prevGui = null;
                };
                bookGui = null;
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (scheduledTask != null) {
                scheduledTask.run();
                scheduledTask = null;
            }
        }
    }
}
