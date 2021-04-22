package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiMoveStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackOptions extends IOptionPage.SimpleToggleableOptions<BlockTrackerClientHandler> {
    public BlockTrackOptions(IGuiScreen screen, BlockTrackerClientHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, settingsYposition() + 12, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.BLOCK_TRACKER));
        }));

        ResourceLocation blockTrackerID = ArmorUpgradeRegistry.getInstance().blockTrackerHandler.getID();

        int nWidgets = BlockTrackEntryList.INSTANCE.trackList.size();
        ResourceLocation owningId = getClientUpgradeHandler().getCommonHandler().getID();
        for (int i = 0; i < nWidgets; i++) {
            WidgetKeybindCheckBox checkBox = WidgetKeybindCheckBox.getOrCreate(BlockTrackEntryList.INSTANCE.trackList.get(i).getEntryID(), 5, 38 + i * 12, 0xFFFFFFFF, cb -> {
                ResourceLocation subID = ((WidgetKeybindCheckBox) cb).getUpgradeId();
                HUDHandler.getInstance().addFeatureToggleMessage(ArmorUpgradeRegistry.getStringKey(blockTrackerID), ArmorUpgradeRegistry.getStringKey(subID), cb.checked);
            }).withOwnerUpgradeID(owningId);
            gui.addWidget(checkBox);
        }
    }

    public boolean displaySettingsHeader() {
        return true;
    }

    @Override
    public int settingsYposition() {
        return 50 + 12 * BlockTrackEntryList.INSTANCE.trackList.size();
    }
}
