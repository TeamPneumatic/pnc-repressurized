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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.ICheckboxWidget;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiMoveStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackOptions extends IOptionPage.SimpleOptionPage<BlockTrackerClientHandler> {
    public BlockTrackOptions(IGuiScreen screen, BlockTrackerClientHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, settingsYposition() + 12, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeContainer();
            Minecraft.getInstance().setScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.BLOCK_TRACKER));
        }));

        ResourceLocation blockTrackerID = ArmorUpgradeRegistry.getInstance().blockTrackerHandler.getID();

        int nWidgets = BlockTrackEntryList.INSTANCE.trackList.size();
        ResourceLocation owningId = getClientUpgradeHandler().getCommonHandler().getID();
        IPneumaticHelmetRegistry registry = PneumaticRegistry.getInstance().getHelmetRegistry();
        for (int i = 0; i < nWidgets; i++) {
            ICheckboxWidget checkBox = registry.makeKeybindingCheckBox(
                    BlockTrackEntryList.INSTANCE.trackList.get(i).getEntryID(), 5, 38 + i * 12, 0xFFFFFFFF, cb -> {
                        ResourceLocation subID = cb.getUpgradeId();
                        HUDHandler.getInstance().addFeatureToggleMessage(ArmorUpgradeRegistry.getStringKey(blockTrackerID), ArmorUpgradeRegistry.getStringKey(subID), cb.isChecked());
                    }).withOwnerUpgradeID(owningId);
            gui.addWidget(checkBox.asWidget());
        }
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }

    @Override
    public int settingsYposition() {
        return 50 + 12 * BlockTrackEntryList.INSTANCE.trackList.size();
    }
}
