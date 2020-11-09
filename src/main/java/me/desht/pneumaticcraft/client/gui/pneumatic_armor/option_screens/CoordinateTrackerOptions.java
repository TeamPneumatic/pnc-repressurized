package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoordTrackClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collections;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CoordinateTrackerOptions extends IOptionPage.SimpleToggleableOptions<CoordTrackClientHandler> {
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
        pathEnabled = new WidgetButtonExtended(30, 128, 150, 20, StringTextComponent.EMPTY,
                b -> {
                    coordHandler.pathEnabled = !coordHandler.pathEnabled;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        wirePath = new WidgetButtonExtended(30, 150, 150, 20, StringTextComponent.EMPTY,
                b -> {
                    coordHandler.wirePath = !coordHandler.wirePath;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        xRayEnabled = new WidgetButtonExtended(30, 172, 150, 20, StringTextComponent.EMPTY,
                b -> {
                    coordHandler.xRayEnabled = !coordHandler.xRayEnabled;
                    updateButtonTexts();
                    coordHandler.saveToConfig();
                });
        pathUpdateRate = new WidgetButtonExtended(30, 194, 150, 20, StringTextComponent.EMPTY,
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
        mc.player.closeScreen();
        mc.setGameFocused(true);
        coordHandler.isListeningToCoordTrackerSetting = true;
        HUDHandler.getInstance().addMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.settingCoord"),
                Collections.singletonList(xlate("pneumaticcraft.armor.message.coordinateTracker.rightClickToSet")),
                90, 0x7000AA00);
    }

    private void navigateToSurface() {
        mc.player.closeScreen();
        mc.setGameFocused(true);
        switch (coordHandler.navigateToSurface(mc.player)) {
            case EASY_PATH:
                HUDHandler.getInstance().addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.routeFound"), new ArrayList<>(), 90, 0x7000AA00));
                break;
            case DRONE_PATH:
                HUDHandler.getInstance().addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.harderRouteFound"), new ArrayList<>(), 90, 0x7044AA00));
                break;
            case NO_PATH:
                HUDHandler.getInstance().addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.coordinateTracker.noRouteFound"), new ArrayList<>(), 90, 0x70FF0000));
                break;
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
