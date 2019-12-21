package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoordTrackUpgradeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collections;

public class GuiCoordinateTrackerOptions extends IOptionPage.SimpleToggleableOptions<CoordTrackUpgradeHandler> {
    private Button wirePath;
    private Button pathEnabled;
    private Button xRayEnabled;
    private Button pathUpdateRate;
    private final CoordTrackUpgradeHandler coordHandler = HUDHandler.instance().getSpecificRenderer(CoordTrackUpgradeHandler.class);
    private final Minecraft mc = Minecraft.getInstance();

    public GuiCoordinateTrackerOptions(IGuiScreen screen, CoordTrackUpgradeHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 40, 150, 20,
                "Select Target...", b -> selectTarget()));
        gui.addWidget(new WidgetButtonExtended(30, 62, 150, 20,
                "Navigate to Surface...", b -> navigateToSurface()));
        pathEnabled = new WidgetButtonExtended(30, 128, 150, 20, "",
                b -> {
                    coordHandler.pathEnabled = !coordHandler.pathEnabled;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        wirePath = new WidgetButtonExtended(30, 150, 150, 20, "",
                b -> {
                    coordHandler.wirePath = !coordHandler.wirePath;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        xRayEnabled = new WidgetButtonExtended(30, 172, 150, 20, "",
                b -> {
                    coordHandler.xRayEnabled = !coordHandler.xRayEnabled;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        pathUpdateRate = new WidgetButtonExtended(30, 194, 150, 20, "",
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
     * See also: {@link CoordTrackUpgradeHandler#onPlayerInteract(PlayerInteractEvent.RightClickBlock)}
     */
    private void selectTarget() {
        mc.player.closeScreen();
        mc.setGameFocused(true);
        coordHandler.isListeningToCoordTrackerSetting = true;
        HUDHandler.instance().addMessage(new ArmorMessage("Changing Coordinate Tracker coordinate...",
                Collections.singletonList("Right-click the desired coordinate"), 90, 0x7000AA00)
        );
    }

    private void navigateToSurface() {
        mc.player.closeScreen();
        mc.setGameFocused(true);
        switch (coordHandler.navigateToSurface(mc.player)) {
            case EASY_PATH:
                HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("pneumaticHelmet.message.coordinateTracker.routeFound"), new ArrayList<>(), 90, 0x7000AA00));
                break;
            case DRONE_PATH:
                HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("pneumaticHelmet.message.coordinateTracker.harderRouteFound"), new ArrayList<>(), 90, 0x7044AA00));
                break;
            case NO_PATH:
                HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("pneumaticHelmet.message.coordinateTracker.noRouteFound"), new ArrayList<>(), 90, 0x70FF0000));
                break;
        }
    }

    private void updateButtonTexts() {
        CoordTrackUpgradeHandler coordHandler = HUDHandler.instance().getSpecificRenderer(CoordTrackUpgradeHandler.class);
        pathEnabled.setMessage(coordHandler.pathEnabled ? "Navigation Enabled" : "Navigation Disabled");
        wirePath.setMessage(coordHandler.wirePath ? "Wire Navigation" : "Tile Navigation");
        xRayEnabled.setMessage(coordHandler.xRayEnabled ? "X-Ray Enabled" : "X-Ray Disabled");
        switch (coordHandler.pathUpdateSetting) {
            case SLOW:
                pathUpdateRate.setMessage("Path update rate: Low");
                break;
            case NORMAL:
                pathUpdateRate.setMessage("Path update rate: Normal");
                break;
            case FAST:
                pathUpdateRate.setMessage("Path update rate: Fast");
                break;
        }
        wirePath.active = coordHandler.pathEnabled;
        xRayEnabled.active = coordHandler.pathEnabled;
        pathUpdateRate.active = coordHandler.pathEnabled;
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
