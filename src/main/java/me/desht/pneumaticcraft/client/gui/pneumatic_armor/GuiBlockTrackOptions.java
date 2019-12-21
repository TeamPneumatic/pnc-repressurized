package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import net.minecraft.client.Minecraft;

public class GuiBlockTrackOptions extends IOptionPage.SimpleToggleableOptions<BlockTrackUpgradeHandler> {
    public GuiBlockTrackOptions(IGuiScreen screen, BlockTrackUpgradeHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, settingsYposition() + 12, 150, 20,
                "Move Stat Screen...", b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getUpgradeHandler(), ArmorHUDLayout.LayoutTypes.BLOCK_TRACKER));
        }));

        int nWidgets = BlockTrackEntryList.instance.trackList.size();
        for (int i = 0; i < nWidgets; i++) {
            WidgetKeybindCheckBox checkBox = new WidgetKeybindCheckBox(5, 38 + i * 12, 0xFFFFFFFF,
                    BlockTrackEntryList.instance.trackList.get(i).getEntryName(), cb -> {
                if (cb == WidgetKeybindCheckBox.fromKeyBindingName(cb.getMessage())) {
                    HUDHandler.instance().addFeatureToggleMessage(getUpgradeHandler(), cb.getMessage(), cb.checked);
                }
            });
            gui.addWidget(checkBox);
        }
    }

    public boolean displaySettingsHeader() {
        return true;
    }

    @Override
    public int settingsYposition() {
        return 44 + 12 * BlockTrackEntryList.instance.trackList.size();
    }
}
