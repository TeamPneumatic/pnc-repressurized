package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.IWidgetListener;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiBlockTrackOptions extends IOptionPage.SimpleToggleableOptions implements IWidgetListener {
    public GuiBlockTrackOptions(BlockTrackUpgradeHandler renderHandler) {
        super(renderHandler);
    }

    @Override
    public String getPageName() {
        return "Block Tracker";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        int nWidgets = BlockTrackEntryList.instance.trackList.size();
        gui.getButtonList().add(new GuiButton(10, 30, settingsYposition() + 12, 150, 20, "Move Stat Screen..."));
        for (int i = 0; i < nWidgets; i++) {
            GuiKeybindCheckBox checkBox = new GuiKeybindCheckBox(i, 5, 38 + i * 12, 0xFFFFFFFF, BlockTrackEntryList.instance.trackList.get(i).getEntryName());
            ((GuiHelmetMainScreen) gui).addWidget(checkBox);
            checkBox.setListener(this);
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            Minecraft.getMinecraft().player.closeScreen();
            Minecraft.getMinecraft().displayGuiScreen(new GuiMoveStat(getRenderHandler()));
        }
    }

    @Override
    public boolean displaySettingsText() {
        return true;
    }

    @Override
    public int settingsYposition() {
        return 44 + 12 * BlockTrackEntryList.instance.trackList.size();
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        if (widget instanceof GuiKeybindCheckBox) {
            GuiKeybindCheckBox checkBox = (GuiKeybindCheckBox) widget;
            if (checkBox == GuiKeybindCheckBox.fromKeyBindingName(checkBox.text)) {
                HUDHandler.instance().addFeatureToggleMessage(getRenderHandler(), checkBox.text, checkBox.checked);
            }
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
    }
}
