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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.CoordTrackClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Collections;

import static me.desht.pneumaticcraft.api.client.pneumatic_helmet.IClientArmorRegistry.DEFAULT_MESSAGE_BGCOLOR;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CoordinateTrackerOptions extends IOptionPage.SimpleOptionPage<CoordTrackClientHandler> {
    private Button wirePath;
    private Button pathEnabled;
    private Button xRayEnabled;
    private Button pathUpdateRate;
    private final CoordTrackClientHandler coordHandler = getClientUpgradeHandler();
    private final Minecraft mc = Minecraft.getInstance();

    public CoordinateTrackerOptions(IGuiScreen screen, CoordTrackClientHandler clientHandler) {
        super(screen, clientHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 40, 150, 20,
                xlate("pneumaticcraft.armor.gui.coordinateTracker.selectTarget"), b -> selectTarget()));
        gui.addWidget(new WidgetButtonExtended(30, 62, 150, 20,
                xlate("pneumaticcraft.armor.gui.coordinateTracker.navigateToSurface"), b -> navigateToSurface()));
        pathEnabled = new WidgetButtonExtended(30, 128, 150, 20, Component.empty(),
                b -> {
                    coordHandler.pathEnabled = !coordHandler.pathEnabled;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        wirePath = new WidgetButtonExtended(30, 150, 150, 20, Component.empty(),
                b -> {
                    coordHandler.wirePath = !coordHandler.wirePath;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        xRayEnabled = new WidgetButtonExtended(30, 172, 150, 20, Component.empty(),
                b -> {
                    coordHandler.xRayEnabled = !coordHandler.xRayEnabled;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        pathUpdateRate = new WidgetButtonExtended(30, 194, 150, 20, Component.empty(),
                b -> {
                    coordHandler.pathUpdateSetting = coordHandler.pathUpdateSetting.cycle();
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        gui.addWidget(pathEnabled);
        gui.addWidget(wirePath);
        gui.addWidget(xRayEnabled);
        gui.addWidget(pathUpdateRate);
        updateButtonTexts();
    }

    /**
     * See also: {@link CoordTrackClientHandler.Listener#onPlayerInteract(PlayerInteractEvent.RightClickBlock)}
     */
    private void selectTarget() {
        ClientUtils.getClientPlayer().closeContainer();
        mc.setWindowActive(true);
        coordHandler.isListeningToCoordTrackerSetting = true;
        HUDHandler.getInstance().addMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.settingCoord"),
                Collections.singletonList(xlate("pneumaticcraft.armor.message.coordinateTracker.rightClickToSet")),
                90, DEFAULT_MESSAGE_BGCOLOR);
    }

    private void navigateToSurface() {
        ClientUtils.getClientPlayer().closeContainer();
        mc.setWindowActive(true);
        switch (coordHandler.navigateToSurface(ClientUtils.getClientPlayer())) {
            case EASY_PATH -> HUDHandler.getInstance().addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.routeFound"), 90, DEFAULT_MESSAGE_BGCOLOR));
            case DRONE_PATH -> HUDHandler.getInstance().addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.harderRouteFound"), 90, 0x7044AA00));
            case NO_PATH -> HUDHandler.getInstance().addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.noRouteFound"), 90, 0x70FF0000));
        }
    }

    private void updateButtonTexts() {
        pathEnabled.setMessage(xlate("pneumaticcraft.armor.gui.coordinateTracker.navEnabled." + coordHandler.pathEnabled));
        wirePath.setMessage(xlate("pneumaticcraft.armor.gui.coordinateTracker.wirePath." + coordHandler.wirePath));
        xRayEnabled.setMessage(xlate("pneumaticcraft.armor.gui.coordinateTracker.xray." + coordHandler.xRayEnabled));
        pathUpdateRate.setMessage(xlate(coordHandler.pathUpdateSetting.getTranslationKey()));

        wirePath.active = coordHandler.pathEnabled;
        xRayEnabled.active = coordHandler.pathEnabled;
        pathUpdateRate.active = coordHandler.pathEnabled;
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
